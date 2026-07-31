# Courier Tracking

A REST service that ingests courier geolocations, logs when a courier comes within 100 m of a
store, and reports the total distance a courier has travelled.

## Stack

- Java 26, Spring Boot **4.1.0** (Spring Framework **7.0.8**)
- Spring Data JPA with in-memory H2, Spring Data Redis

## Requirements

- **JDK 26**
- Docker (optional — only for Redis)

## Run

Everything in Docker:

```bash
docker compose up --build
```

Or run the app from Gradle with Redis in Docker:

```bash
docker compose up redis
./gradlew bootRun
```

The app listens on `http://localhost:8080` and uses H2 by default. Redis is optional; see
*Store entrances* below.

The H2 console is at `/h2-console`:

```
JDBC URL:  jdbc:h2:mem:courier
User:      sa
Password:  (empty)
```

## API

### Send a location

```bash
curl -X POST localhost:8080/api/v1/couriers/locations \
     -H 'Content-Type: application/json' \
     -d '{"timeSeconds":1700000000,"courierId":1,"latitude":40.9927,"longitude":29.1244229}'
```

`timeSeconds` is epoch seconds. Returns the stored location. Invalid payloads come back as
`400` with an RFC 9457 body listing the rejected fields.

### Total travel distance

```bash
curl localhost:8080/api/v1/couriers/1/total-distance
# {"courierId":1,"totalDistanceMeters":1234.56}
```

Sums the distance between consecutive positions, ordered by event time.

## Store entrances

When a location falls within **100 m** of a store from `stores.json`, an entrance is logged.
A re-entry to the same store **within 1 minute is not counted** as a new entrance.

The window is measured against the `timeSeconds` in the request, not the server clock — replaying or
delaying a stream produces the same entrances as a live one.

Redis caches the last entrance per courier and store. It is a cache, not the source of truth: on a
cache miss — or when Redis is down — the answer comes from the database, so entrances are never
lost.

Try it (all three requests can be sent back to back; only the event times matter):

```bash
B=localhost:8080/api/v1/couriers
P='"courierId":1,"latitude":40.9927,"longitude":29.1244229'
curl -X POST $B/locations -H 'Content-Type: application/json' -d "{\"timeSeconds\":1700000000,$P}"  # entrance
curl -X POST $B/locations -H 'Content-Type: application/json' -d "{\"timeSeconds\":1700000030,$P}"  # +30s, ignored
curl -X POST $B/locations -H 'Content-Type: application/json' -d "{\"timeSeconds\":1700000090,$P}"  # +90s, entrance
```

Entrances are written to `courier_entrance_log` and logged at INFO with the courier id, the
matched store and the event time.

## Configuration

| Property | Default | Meaning |
|---|---|---|
| `courier.stores-location` | `classpath:stores.json` | Where the store catalogue is read from |
| `courier.distance.strategy` | `HAVERSINE` | Distance formula: `HAVERSINE`, `EQUIRECTANGULAR` or `VINCENTY` |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Cache location |

## Tests

```bash
./gradlew build
```

11 tests. Unit tests cover the 100 m gate, both branches of the 1-minute rule, the database
fallback when Redis is down, and catalogue parsing. Integration tests run the full context —
`POST /locations` through the async listener into `courier_entrance_log`, the total-distance
endpoint, and request validation.

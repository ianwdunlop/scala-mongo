# Scala Mongo

MongoDB Abstraction for ease of connection and access.

## Usage
When connecting to a replica set you can either:
1. specify a comma delimited list of hosts, or
2. use an srv record, by setting `MONGO_SRV=true`. If you supplied more than one host in this case, only the first host is used in DNS resolution.

Any codecs must be declared implicitly.

The config from which connection details are pulled must be declared implicitly.

## Config
The default is to read from the secondary with default replication lag of 2 seconds in `mongo.connection.maxStaleness`.
You can change `mongo.connnection.readPreference` to `primaryPreferred` if you want which then ignores the staleness setting.

## Testing
```bash
docker-compose up -d
sbt clean it:test
```

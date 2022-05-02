# pdf4oereb-web-service

## Build
```
./gradle clean build
```

## Run 
```
java -jar build/libs/pdf4oereb-web-service-X.X.X.jar
```

## Docker
```
docker build -t sogis/pdf4oereb .
```

```
docker run -p 8080:8080 sogis/pdf4oereb
```

## API
```
curl -XPOST -F language=it -F file=@CH344982777421_geometry_wms.xml http://localhost:8080/convert --output CH344982777421_geometry_wms.pdf
```

## Links

- https://github.com/sogis/pdf4oereb

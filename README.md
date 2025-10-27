Запуск:
```
java -jar target/crypto-1.0-SNAPSHOT-jar-with-dependencies.jar --mode encrypt|decrypt --in img.png --out out.png \
                                                               --algo stream|perm-mix \
                                                               --key "секрет" [--iv HEX] [--meta META.json]
```

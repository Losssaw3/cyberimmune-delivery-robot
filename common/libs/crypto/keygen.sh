#!/bin/bash

SIGN_ALIAS=taskSign
PIN_ALIAS=pinSecret

KEYSTORE_SERVER=server_sign_keystore.p12
KEYSTORE_SERVER_PASS=ahmai6oacaitioNg3requohk9OeHijoo
KEYSTORE_CLIENT=client_sign_keystore.p12
KEYSTORE_CLIENT_PASS=uogh7noh4Ree8huZ9shae7vi6ohphohj

KEYSTORE_SECRET=secret_keystore.p12
KEYSTORE_SECRET_PASS=kiuw2ka7ahSeeTh2wieb6ohy1Xu3haj4

rm -f $KEYSTORE_SERVER $KEYSTORE_CLIENT $KEYSTORE_SECRET

keytool -genkeypair -alias $SIGN_ALIAS -keyalg RSA -keysize 2048 -dname "CN=ITMO" -validity 1000 -storetype PKCS12 -keystore $KEYSTORE_SERVER -storepass $KEYSTORE_SERVER_PASS
keytool -exportcert -alias $SIGN_ALIAS -storetype PKCS12 -keystore $KEYSTORE_SERVER -file sign.cer -rfc -storepass $KEYSTORE_SERVER_PASS
keytool -importcert -noprompt -alias $SIGN_ALIAS -storetype PKCS12 -keystore $KEYSTORE_CLIENT -file sign.cer -rfc -storepass $KEYSTORE_CLIENT_PASS
rm sign.cer

#keytool -genseckey -alias $PIN_ALIAS -keyalg AES -keysize 256 -keystore $KEYSTORE_SECRET -storetype PKCS12 -storepass $KEYSTORE_SECRET_PASS


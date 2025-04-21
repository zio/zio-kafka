# Keystore and truststore files generation

The following files are used in the testkit. They were generated thanks to https://github.com/confluentinc/confluent-platform-security-tools/blob/master/kafka-generate-ssl.sh:

 - `keystore/kafka.keystore.jks`
   - View with `keytool -list -v -keystore keystore/kafka.keystore.jks`
   - Password: `123456`
   - Entry 1
     - alias: `caroot`
     - owner: `O=Internet Widgits Pty Ltd, ST=Some-State, C=AU`
     - valid until 2033-01-16
   - Entry 2
     - alias: `localhost`
     - owner: `CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown`
     - valid until 2033-01-16
 - `truststore/kafka.truststore.jks`
   - View with `keytool -list -v -keystore truststore/kafka.truststore.jks`
   - Password: `123456`
   - Keystore type: `PKCS12`
   - Entry 1
     - alias: `caroot`
     - Owner: `O=Internet Widgits Pty Ltd, ST=Some-State, C=AU`
     - valid until 2033-01-16
 - `truststore/ca-key`
   - View with `openssl rsa -in truststore/ca-key -text`
   - RSA certificate
   - Password: `123456`

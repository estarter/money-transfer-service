# Money transfer service

How to start the application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/money-transfer-service-1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`

Usage example
---

Here's an example of usage, [http](https://httpie.org/) program is used as an alternative to curl. 

```bash
# create new account
http :8080/api/accounts name=test_acc1 currency=CHF balance=100 -v
# see all account's ids
http :8080/api/accounts
# see the account
http :8080/api/accounts/1

http :8080/api/accounts name=test_acc2 currency=CHF balance=200 -v

http :8080/api/transactions from=2 to=1 amount=10 currency=CHF

```

List of operational tools is available at [admin page](http://localhost:8081/).
It includes health check, metrics, and others. 


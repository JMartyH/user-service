# user-service
## Summary
This is a spring boot rest application for registering users. Still plenty of TODOs.

## How to run
- Need to have MySQL running (I have it running via XAMPP)
- The application default port is 8080
- You need to have FakeSMTP running on port 25 to simulate sending welcome email
- Test the endpoints via POSTMAN

## TODOs
- JWT authentication
  - Currently csrf is disabled to be able to access the endpoints.
- Frontend
- Building the application in Docker to be able to deploy on a container.

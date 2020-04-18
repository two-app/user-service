# User API
Internal REST endpoints to retrieve user data.

## Endpoints
| Verb | Endpoint | Description                     | Input                                  | Output            |
|------|----------|---------------------------------|----------------------------------------|-------------------|
| GET | `/user?email={}`  | Retrieve a user by email.            | [*Required* Query Param 'email'](#Email) | [User](#User), NotFoundError, ClientError |

## Models
### Email
| Attribute      | Type           | Contraints                           |
|----------------|----------------|--------------------------------------|
| email          | `string`       | unique && https://stackoverflow.com/a/32445372 |


### User
| Attribute | Type          |
|-----------|---------------|
| uid       | `int`         |
| pid       | `Option[int]` |
| cid       | `Option[int]` |
| firstName | `string`      |
| lastName  | `string`      |
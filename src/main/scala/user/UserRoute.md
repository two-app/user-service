# User API
Internal REST endpoints to retrieve user data.

## Endpoints
| Verb | Endpoint | Description                     | Input                                  | Output            |
|------|----------|---------------------------------|----------------------------------------|-------------------|
| GET | `/user?email={}` | Retrieve a user by email. | [*Required* Query Param 'email'](#Email) | [User](#User), NotFoundError, ClientError |
| GET | `/user?uid={}` | Retrieve a user by uid. | [*Required* Query Param 'uid'](#UID) | [User](#User), NotFoundError, ClientError |

## Models
### Email
| Attribute      | Type           | Contraints                           |
|----------------|----------------|--------------------------------------|
| email          | `string`       | unique && https://stackoverflow.com/a/32445372 |

### UID
| Attribute      | Type           | Contraints                           |
|----------------|----------------|--------------------------------------|
| uid            | `int`          | unique && > 0 |


### User
| Attribute | Type          |
|-----------|---------------|
| uid       | `int`         |
| pid       | `Option[int]` |
| cid       | `Option[int]` |
| firstName | `string`      |
| lastName  | `string`      |
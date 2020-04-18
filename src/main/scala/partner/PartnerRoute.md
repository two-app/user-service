# Partner API
The partner endpoint provides REST endpoints to connect with a user, or retrieve partners information.

## Endpoints
| Verb | Endpoint | Description                     | Input                                  | Output            |
|------|----------|---------------------------------|----------------------------------------|-------------------|
| POST | `/partner/{connectCode}` | Connect to a new partner. | [ConnectCode](#ConnectCode)  | [Tokens](#Tokens), ClientError |
| GET  | `/partner`  | Retrieve user from PID in Auth header. |                              | [User](#User)     |

## Models
### ConnectCode
| Attribute      | Type           | Contraints                           |
|----------------|----------------|--------------------------------------|
| connectCode | `string` | length >= 6, is valid according to hash |

### User
| Attribute | Type          |
|-----------|---------------|
| uid       | `int`         |
| pid       | `Option[int]` |
| cid       | `Option[int]` |
| firstName | `string`      |
| lastName  | `string`      |
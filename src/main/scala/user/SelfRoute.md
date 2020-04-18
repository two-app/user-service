# Self API
The self API provides REST endpoints to register and retrieve ones self.

## Endpoints
| Verb | Endpoint | Description                     | Input                                  | Output            |
|------|----------|---------------------------------|----------------------------------------|-------------------|
| POST | `/self`  | Register a new user.            | [UserRegistration](#User-Registration) | [Tokens](#Tokens), ClientError |
| GET  | `/self`  | Retrieve user from Auth header. |                                        | [User](#User)     |

## Models
### User Registration
| Attribute      | Type           | Contraints                           |
|----------------|----------------|--------------------------------------|
| firstName      | `string`       | length >= 2                          |
| lastName       | `string`       | length >= 2                          |
| email          | `string`       | unique && https://stackoverflow.com/a/32445372 |
| password       | `string`       | length >= 6                          |
| acceptedTerms  | `bool`         | true                              |
| ofAge          | `bool`         | true                              |
| receivesEmails | `Option[bool]` |                                      |

#### Example
```json
{
	"firstName": "First",
	"lastName": "Last",
	"email": "newuser@two.com",
	"password": "SomethingStrong",
	"acceptedTerms": true,
	"ofAge": true,
	"receivesEmails": false
}
```
In this example, `receiveEmails` can simply be omitted as it is `Optional`.

### User
| Attribute | Type          |
|-----------|---------------|
| uid       | `int`         |
| pid       | `Option[int]` |
| cid       | `Option[int]` |
| firstName | `string`      |
| lastName  | `string`      |

#### Example
Typical User
```json
{
	"uid": 12,
	"pid": 39,
	"cid": 44,
	"firstName": "Typical",
	"lastName": "User"
}
```

Pre-Connection User
```json
{
	"uid": 10,
	"firstName": "Preconnection",
	"lastName": "User"
}
```

### Tokens
More information on tokens in the [Authentication Service](https://github.com/two-app/authentication-service).

| Attribute    | Type     |
|--------------|----------|
| accessToken  | `string` |
| refreshToken | `string` |

#### Example
```json
{
    "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0d28iLCJ1aWQiOiA0NywgImNvbm5lY3RDb2RlIjogInFyZEE3VyIsICJyb2xlIjogIkNPTk5FQ1QifQ.oklLsOLA63KPnJbtqzYGLJCDCDNrfspcavBlQ7Dgvbg",
    "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ0d28iLCJ1aWQiOiA0NywgInJvbGUiOiAiUkVGUkVTSCJ9.vTA_wMzwxhxHskJEWRkR3azeKFvf3S5TUG_YaC2QYFY"
}
```
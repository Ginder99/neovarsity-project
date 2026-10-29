# Notification Service

The notification service sends email notifications through SendGrid using
file-based templates.

## Endpoint

`POST /api/v1/notifications`

```json
{
  "notificationType": "order_confirmation",
  "recipientEmail": "user@example.com"
}
```

The endpoint returns `202 Accepted` with `{"status":"queued"}` after SendGrid
accepts the email. Templates are loaded from
`src/main/resources/email-templates/<notificationType>/subject.txt` and
`content.txt`.

## Configuration

- `SENDGRID_API_KEY` - SendGrid API key
- `SENDGRID_FROM_EMAIL` - verified sender address

Run locally with:

```bash
./gradlew clean build
./gradlew bootRun
```

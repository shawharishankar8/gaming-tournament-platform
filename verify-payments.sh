#!/bin/bash
echo "=== PAYMENT SYSTEM FINAL VERIFICATION ==="

JWT="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc2MjA4Mjc1NSwiZXhwIjoxNzYyMTY5MTU1fQ.SniLhSfHpj8fxLrxiDDkIksBQe44ln9dLH7DXa6D0BI"

echo "1. Database Records:"
docker exec tournament-postgres psql -U postgres -d tournament_platform -c "SELECT COUNT(*) as payment_count FROM payments;"

echo -e "\n2. Health Check:"
curl -s http://localhost:8080/actuator/health | jq '.status'

echo -e "\n3. Payment Intent Creation:"
response=$(curl -s -X POST http://localhost:8080/api/payments/create-intent \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT" \
  -d '{"tournamentId":1,"amount":1.00}')
echo $response | jq '.status'

echo -e "\n4. Error Handling:"
curl -s -X POST http://localhost:8080/api/payments/create-intent \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT" \
  -d '{"tournamentId":999,"amount":25.00}' | jq '.error'

echo -e "\n✅ PAYMENT SYSTEM VERIFICATION COMPLETE!"

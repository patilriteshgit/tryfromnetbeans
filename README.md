
1. `genreqcount` Servlet:
- This servlet handles the generation of OTP (One-Time Password) for a given Aadhar number.
- It reads JSON data from the request body, extracts the Aadhar number, and checks if the request limit has been exceeded.
- If the request limit is not exceeded, it generates a random OTP, saves it along with the Aadhar number in the database, and sends the OTP as a JSON response.

2. `valreqcount` Servlet:
- This servlet handles the validation of OTP for a given Aadhar number.
- It reads JSON data from the request body, extracts the Aadhar number and OTP, and checks if the request limit has been exceeded.
- If the request limit is not exceeded, it retrieves the OTP from the database and validates it against the provided OTP.
- It sends a JSON response indicating whether the authentication was successful or not, and deletes the OTP record if authentication was successful.

3. `OTP` Entity:
- This entity class represents the OTP information stored in the database.
- It has fields such as `aadharNumber`, `otp`, `lastRequestTime`, `genrequestCount`, and `valrequestCount`.
- The `aadharNumber` field is annotated with `@Id`, indicating it as the primary key.
- The `otp` field stores the generated OTP.
- The `lastRequestTime` field stores the timestamp of the last request made for this OTP.
- The `genrequestCount` field keeps track of the number of generation requests made for this OTP.
- The `valrequestCount` field keeps track of the number of validation requests made for this OTP.

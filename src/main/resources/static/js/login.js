const loginForm = document.getElementById("loginForm");
const loginButton = document.getElementById("loginButton");
const loginMessage = document.getElementById("loginMessage");

loginForm.addEventListener("submit", async event => {
  event.preventDefault();
  loginMessage.className = "form-message";
  loginButton.disabled = true;
  loginButton.textContent = "Signing in...";

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;
  const token = btoa(email + ":" + password);

  try {
    const loginResponse = await fetch("/login", {
      headers: { "Authorization": "Basic " + token }
    });

    if (!loginResponse.ok) {
      throw new Error("Incorrect email or password");
    }

    sessionStorage.setItem(BankApp.tokenKey, token);
    const profileResponse = await BankApp.secureFetch("/customer/me");
    const user = await BankApp.readResponse(profileResponse);
    if (!profileResponse.ok || typeof user !== "object") {
      throw new Error(BankApp.messageText(user));
    }

    BankApp.setLogin(email, password, user);
    location.href = user.role === "ADMIN" ? "/admin.html" : "/dashboard.html";
  } catch (error) {
    sessionStorage.removeItem(BankApp.tokenKey);
    sessionStorage.removeItem(BankApp.userKey);
    loginMessage.textContent = error.message;
    loginMessage.className = "form-message error show";
  } finally {
    loginButton.disabled = false;
    loginButton.textContent = "Sign in securely";
  }
});

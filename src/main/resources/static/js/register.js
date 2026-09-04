const registerForm = document.getElementById("registerForm");
const registerButton = document.getElementById("registerButton");
const registerMessage = document.getElementById("registerMessage");

registerForm.addEventListener("submit", async event => {
  event.preventDefault();
  registerMessage.className = "form-message field full";
  registerButton.disabled = true;
  registerButton.textContent = "Creating profile...";

  const customer = {
    name: document.getElementById("name").value.trim(),
    email: document.getElementById("email").value.trim(),
    phone: Number(document.getElementById("phone").value),
    address: document.getElementById("address").value.trim(),
    password: document.getElementById("password").value
  };

  try {
    const response = await fetch("/customer/save", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(customer)
    });
    const data = await BankApp.readResponse(response);

    if (!response.ok || data !== "Customer saved successfully") {
      throw new Error(BankApp.messageText(data));
    }

    registerMessage.textContent = "Profile created successfully. Redirecting to sign in...";
    registerMessage.className = "form-message field full success show";
    registerForm.reset();
    setTimeout(() => location.href = "/index.html", 1300);
  } catch (error) {
    registerMessage.textContent = error.message;
    registerMessage.className = "form-message field full error show";
  } finally {
    registerButton.disabled = false;
    registerButton.textContent = "Create secure profile";
  }
});

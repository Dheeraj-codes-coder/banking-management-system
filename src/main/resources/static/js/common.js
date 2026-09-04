const BankApp = {
  tokenKey: "nexabank_auth",
  userKey: "nexabank_user",

  getToken() {
    return sessionStorage.getItem(this.tokenKey);
  },

  setLogin(email, password, user) {
    sessionStorage.setItem(this.tokenKey, btoa(email + ":" + password));
    sessionStorage.setItem(this.userKey, JSON.stringify(user));
  },

  getUser() {
    try {
      return JSON.parse(sessionStorage.getItem(this.userKey));
    } catch (error) {
      return null;
    }
  },

  authHeaders() {
    return {
      "Authorization": "Basic " + this.getToken(),
      "Content-Type": "application/json"
    };
  },

  async secureFetch(url, options = {}) {
    const headers = Object.assign({}, this.authHeaders(), options.headers || {});
    const response = await fetch(url, Object.assign({}, options, { headers }));
    if (response.status === 401) {
      this.logout();
      throw new Error("Your login has expired. Please sign in again.");
    }
    return response;
  },

  async readResponse(response) {
    const type = response.headers.get("content-type") || "";
    if (type.includes("application/json")) {
      return response.json();
    }
    return response.text();
  },

  messageText(data) {
    if (typeof data === "string") {
      return data;
    }
    if (data && typeof data === "object") {
      return Object.values(data).join(" • ");
    }
    return "Something went wrong";
  },

  currency(value) {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2
    }).format(Number(value || 0));
  },

  formatDate(value) {
    if (!value) {
      return "—";
    }
    return new Intl.DateTimeFormat("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    }).format(new Date(value));
  },

  initials(name) {
    return String(name || "User")
      .split(" ")
      .slice(0, 2)
      .map(part => part.charAt(0))
      .join("")
      .toUpperCase();
  },

  escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value == null ? "" : String(value);
    return div.innerHTML;
  },

  showToast(message, type = "success") {
    let container = document.querySelector(".toast-container");
    if (!container) {
      container = document.createElement("div");
      container.className = "toast-container";
      document.body.appendChild(container);
    }
    const toast = document.createElement("div");
    toast.className = "toast " + type;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3600);
  },

  setLoading(show) {
    const line = document.querySelector(".loading-line");
    if (line) {
      line.classList.toggle("show", show);
    }
  },

  requireLogin(expectedRole) {
    const user = this.getUser();
    if (!this.getToken() || !user) {
      location.href = "/index.html";
      return null;
    }
    if (expectedRole && user.role !== expectedRole) {
      location.href = user.role === "ADMIN" ? "/admin.html" : "/dashboard.html";
      return null;
    }
    return user;
  },

  logout() {
    sessionStorage.removeItem(this.tokenKey);
    sessionStorage.removeItem(this.userKey);
    location.href = "/index.html";
  },

  setupMobileMenu() {
    const button = document.querySelector(".mobile-menu");
    const sidebar = document.querySelector(".sidebar");
    if (button && sidebar) {
      button.addEventListener("click", () => sidebar.classList.toggle("open"));
      document.addEventListener("click", event => {
        if (window.innerWidth <= 820 && !sidebar.contains(event.target) && !button.contains(event.target)) {
          sidebar.classList.remove("open");
        }
      });
    }
  }
};

document.addEventListener("click", event => {
  const toggle = event.target.closest("[data-password-toggle]");
  if (toggle) {
    const input = document.querySelector(toggle.dataset.passwordToggle);
    if (input) {
      input.type = input.type === "password" ? "text" : "password";
      toggle.textContent = input.type === "password" ? "Show" : "Hide";
    }
  }
});

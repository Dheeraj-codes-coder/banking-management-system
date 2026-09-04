const dashboardUser = BankApp.requireLogin("USER");
const dashboardState = {
  user: dashboardUser,
  accounts: [],
  transactions: []
};

function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) {
    modal.classList.add("show");
    modal.setAttribute("aria-hidden", "false");
  }
}

function closeModal(modal) {
  modal.classList.remove("show");
  modal.setAttribute("aria-hidden", "true");
}

function renderProfile() {
  const firstName = dashboardState.user.name.split(" ")[0];
  document.getElementById("heroName").textContent = firstName;
  document.getElementById("profileName").textContent = dashboardState.user.name;
  document.getElementById("profileEmail").textContent = dashboardState.user.email;
  document.getElementById("profileAvatar").textContent = BankApp.initials(dashboardState.user.name);
  document.getElementById("todayText").textContent = new Intl.DateTimeFormat("en-IN", {
    weekday: "long", day: "numeric", month: "long", year: "numeric"
  }).format(new Date());
}

function renderAccounts() {
  const accountList = document.getElementById("accountList");
  const total = dashboardState.accounts.reduce((sum, account) => sum + Number(account.balance), 0);
  document.getElementById("heroBalance").textContent = BankApp.currency(total);
  document.getElementById("totalBalance").textContent = BankApp.currency(total);
  document.getElementById("accountCount").textContent = dashboardState.accounts.length;

  if (!dashboardState.accounts.length) {
    accountList.innerHTML = '<div class="empty-state"><strong>No bank account yet</strong>Open your first account to begin.</div>';
  } else {
    accountList.innerHTML = dashboardState.accounts.map(account => `
      <div class="account-row">
        <div class="account-name">
          <span class="account-symbol">${account.accountType === "CURRENT" ? "CA" : "SA"}</span>
          <div>
            <strong>${BankApp.escapeHtml(account.accountType)} account</strong>
            <span>${BankApp.escapeHtml(account.accountNumber)} · ID ${account.accountId}</span>
          </div>
        </div>
        <div class="account-balance">
          <strong>${BankApp.currency(account.balance)}</strong>
          <span>Available</span>
        </div>
      </div>`).join("");
  }

  document.querySelectorAll(".account-select").forEach(select => {
    select.innerHTML = dashboardState.accounts.length
      ? dashboardState.accounts.map(account => `<option value="${account.accountId}">${BankApp.escapeHtml(account.accountNumber)} — ${BankApp.currency(account.balance)}</option>`).join("")
      : '<option value="">No account available</option>';
  });
}

function renderTransactions() {
  const table = document.getElementById("transactionTable");
  document.getElementById("transactionCount").textContent = dashboardState.transactions.length;
  if (!dashboardState.transactions.length) {
    table.innerHTML = '<tr><td colspan="5"><div class="empty-state"><strong>No transactions yet</strong>Your latest activity will appear here.</div></td></tr>';
    return;
  }

  table.innerHTML = dashboardState.transactions.slice(0, 20).map(transaction => `
    <tr>
      <td>${BankApp.formatDate(transaction.transactionDate)}</td>
      <td>#${transaction.accountId}</td>
      <td>${BankApp.escapeHtml(transaction.description)}</td>
      <td><span class="badge ${transaction.transactionType === "CREDIT" ? "badge-current" : "badge-user"}">${transaction.transactionType}</span></td>
      <td class="${transaction.transactionType === "CREDIT" ? "amount-credit" : "amount-debit"}">${transaction.transactionType === "CREDIT" ? "+" : "−"}${BankApp.currency(transaction.amount)}</td>
    </tr>`).join("");
}

async function loadDashboard() {
  BankApp.setLoading(true);
  try {
    const profileResponse = await BankApp.secureFetch("/customer/me");
    const profile = await BankApp.readResponse(profileResponse);
    if (profile && typeof profile === "object") {
      dashboardState.user = profile;
      sessionStorage.setItem(BankApp.userKey, JSON.stringify(profile));
      renderProfile();
    }

    const accountResponse = await BankApp.secureFetch("/account/my");
    dashboardState.accounts = await accountResponse.json();

    const transactionGroups = await Promise.all(dashboardState.accounts.map(async account => {
      const response = await BankApp.secureFetch("/transaction/my/" + account.accountId);
      const data = await BankApp.readResponse(response);
      return Array.isArray(data) ? data : [];
    }));
    dashboardState.transactions = transactionGroups.flat().sort((a, b) => new Date(b.transactionDate) - new Date(a.transactionDate));
    renderAccounts();
    renderTransactions();
  } catch (error) {
    BankApp.showToast(error.message, "error");
  } finally {
    BankApp.setLoading(false);
  }
}

async function submitOperation(url, method, form, successText) {
  BankApp.setLoading(true);
  try {
    const response = await BankApp.secureFetch(url, { method });
    const data = await BankApp.readResponse(response);
    const message = BankApp.messageText(data);
    if (!response.ok || !message.toLowerCase().includes("success")) {
      throw new Error(message);
    }
    BankApp.showToast(successText || message, "success");
    form.reset();
    const modal = form.closest(".modal");
    if (modal) {
      closeModal(modal);
    }
    await loadDashboard();
  } catch (error) {
    BankApp.showToast(error.message, "error");
  } finally {
    BankApp.setLoading(false);
  }
}

if (dashboardUser) {
  renderProfile();
  BankApp.setupMobileMenu();
  loadDashboard();

  document.querySelectorAll("[data-open-modal]").forEach(button => {
    button.addEventListener("click", () => openModal(button.dataset.openModal));
  });
  document.querySelectorAll("[data-close-modal]").forEach(button => {
    button.addEventListener("click", () => closeModal(button.closest(".modal")));
  });
  document.querySelectorAll(".modal").forEach(modal => {
    modal.addEventListener("click", event => {
      if (event.target === modal) {
        closeModal(modal);
      }
    });
  });
  document.querySelectorAll("[data-logout]").forEach(button => button.addEventListener("click", () => BankApp.logout()));
  document.getElementById("refreshButton").addEventListener("click", loadDashboard);

  const accountForm = document.getElementById("accountForm");
  accountForm.onsubmit = async event => {
    event.preventDefault();
    const account = {
      accountNumber: document.getElementById("newAccountNumber").value.trim(),
      accountType: document.getElementById("newAccountType").value,
      balance: Number(document.getElementById("openingBalance").value)
    };
    BankApp.setLoading(true);
    try {
      const response = await BankApp.secureFetch("/account/my/save", {
        method: "POST",
        body: JSON.stringify(account)
      });
      const data = await BankApp.readResponse(response);
      const message = BankApp.messageText(data);
      if (!response.ok || !message.toLowerCase().includes("success")) {
        throw new Error(message);
      }
      BankApp.showToast(message, "success");
      accountForm.reset();
      closeModal(accountForm.closest(".modal"));
      await loadDashboard();
    } catch (error) {
      BankApp.showToast(error.message, "error");
    } finally {
      BankApp.setLoading(false);
    }
  };

  document.getElementById("depositForm").addEventListener("submit", event => {
    event.preventDefault();
    const accountId = document.getElementById("depositAccount").value;
    const amount = document.getElementById("depositAmount").value;
    submitOperation(`/account/my/deposit/${accountId}/${amount}`, "PATCH", event.target);
  });

  document.getElementById("withdrawForm").addEventListener("submit", event => {
    event.preventDefault();
    const accountId = document.getElementById("withdrawAccount").value;
    const amount = document.getElementById("withdrawAmount").value;
    submitOperation(`/account/my/withdraw/${accountId}/${amount}`, "PATCH", event.target);
  });

  document.getElementById("transferForm").addEventListener("submit", event => {
    event.preventDefault();
    const fromId = document.getElementById("transferFrom").value;
    const toNumber = encodeURIComponent(document.getElementById("transferTo").value.trim());
    const amount = document.getElementById("transferAmount").value;
    submitOperation(`/account/my/transfer/${fromId}/${toNumber}/${amount}`, "PATCH", event.target);
  });
}

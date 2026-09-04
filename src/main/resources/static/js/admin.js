const adminUser = BankApp.requireLogin("ADMIN");
const adminState = {
  customers: [],
  accounts: [],
  transactions: []
};

function renderAdminProfile() {
  document.getElementById("profileName").textContent = adminUser.name;
  document.getElementById("profileEmail").textContent = adminUser.email;
  document.getElementById("profileAvatar").textContent = BankApp.initials(adminUser.name);
}

function renderAdminOverview() {
  const total = adminState.accounts.reduce((sum, account) => sum + Number(account.balance), 0);
  document.getElementById("customerCount").textContent = adminState.customers.filter(customer => customer.role !== "ADMIN").length;
  document.getElementById("accountCount").textContent = adminState.accounts.length;
  document.getElementById("transactionCount").textContent = adminState.transactions.length;
  document.getElementById("totalBalance").textContent = BankApp.currency(total);
  document.getElementById("heroBalance").textContent = BankApp.currency(total);

  const recent = [...adminState.transactions]
    .sort((a, b) => new Date(b.transactionDate) - new Date(a.transactionDate))
    .slice(0, 6);
  document.getElementById("recentTransactionTable").innerHTML = recent.length
    ? recent.map(transaction => `
      <tr>
        <td>${BankApp.formatDate(transaction.transactionDate)}</td>
        <td>#${transaction.accountId}</td>
        <td>${BankApp.escapeHtml(transaction.description)}</td>
        <td><span class="badge ${transaction.transactionType === "CREDIT" ? "badge-current" : "badge-user"}">${transaction.transactionType}</span></td>
        <td class="${transaction.transactionType === "CREDIT" ? "amount-credit" : "amount-debit"}">${transaction.transactionType === "CREDIT" ? "+" : "−"}${BankApp.currency(transaction.amount)}</td>
      </tr>`).join("")
    : '<tr><td colspan="5"><div class="empty-state">No transactions found.</div></td></tr>';
}

async function loadAdminOverview() {
  BankApp.setLoading(true);
  try {
    const [customerResponse, accountResponse, transactionResponse] = await Promise.all([
      BankApp.secureFetch("/customer/fetch"),
      BankApp.secureFetch("/account/fetch"),
      BankApp.secureFetch("/transaction/fetch")
    ]);
    if (!customerResponse.ok || !accountResponse.ok || !transactionResponse.ok) {
      throw new Error("Unable to load administrator data");
    }
    adminState.customers = await customerResponse.json();
    adminState.accounts = await accountResponse.json();
    adminState.transactions = await transactionResponse.json();
    renderAdminOverview();
  } catch (error) {
    BankApp.showToast(error.message, "error");
  } finally {
    BankApp.setLoading(false);
  }
}

if (adminUser) {
  renderAdminProfile();
  BankApp.setupMobileMenu();
  loadAdminOverview();
  document.getElementById("refreshButton").addEventListener("click", loadAdminOverview);
  document.querySelectorAll("[data-logout]").forEach(button => button.addEventListener("click", () => BankApp.logout()));
}

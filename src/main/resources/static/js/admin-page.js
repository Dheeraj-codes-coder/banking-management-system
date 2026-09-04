const pageAdmin = BankApp.requireLogin("ADMIN");
const adminSection = document.body.dataset.adminSection;

function setupAdminPage() {
  document.getElementById("profileName").textContent = pageAdmin.name;
  document.getElementById("profileEmail").textContent = pageAdmin.email;
  document.getElementById("profileAvatar").textContent = BankApp.initials(pageAdmin.name);
  BankApp.setupMobileMenu();
  document.querySelectorAll("[data-logout]").forEach(button => button.addEventListener("click", () => BankApp.logout()));
  document.getElementById("refreshButton").addEventListener("click", loadAdminPage);
}

function renderCustomers(customers) {
  const standardCustomers = customers.filter(customer => customer.role !== "ADMIN");
  document.getElementById("pageHeroValue").textContent = standardCustomers.length;
  document.getElementById("pageTable").innerHTML = customers.length
    ? customers.map(customer => `
      <tr>
        <td>#${customer.customerId}</td>
        <td>${BankApp.escapeHtml(customer.name)}</td>
        <td>${BankApp.escapeHtml(customer.email)}</td>
        <td>${customer.phone}</td>
        <td>${BankApp.escapeHtml(customer.address)}</td>
        <td><span class="badge ${customer.role === "ADMIN" ? "badge-admin" : "badge-user"}">${customer.role || "USER"}</span></td>
        <td>${customer.role === "ADMIN" ? "—" : `<button class="btn btn-danger" data-delete-customer="${customer.customerId}" type="button">Delete</button>`}</td>
      </tr>`).join("")
    : '<tr><td colspan="7"><div class="empty-state">No customers found.</div></td></tr>';
}

function renderAccounts(accounts) {
  const total = accounts.reduce((sum, account) => sum + Number(account.balance), 0);
  document.getElementById("pageHeroValue").textContent = BankApp.currency(total);
  document.getElementById("pageTable").innerHTML = accounts.length
    ? accounts.map(account => `
      <tr>
        <td>#${account.accountId}</td>
        <td>${BankApp.escapeHtml(account.accountNumber)}</td>
        <td><span class="badge ${account.accountType === "CURRENT" ? "badge-current" : "badge-savings"}">${account.accountType}</span></td>
        <td>#${account.customerId}</td>
        <td><strong>${BankApp.currency(account.balance)}</strong></td>
      </tr>`).join("")
    : '<tr><td colspan="5"><div class="empty-state">No accounts found.</div></td></tr>';
}

function renderTransactions(transactions) {
  const ordered = [...transactions].sort((a, b) => new Date(b.transactionDate) - new Date(a.transactionDate));
  document.getElementById("pageHeroValue").textContent = ordered.length;
  document.getElementById("pageTable").innerHTML = ordered.length
    ? ordered.map(transaction => `
      <tr>
        <td>#${transaction.transactionId}</td>
        <td>${BankApp.formatDate(transaction.transactionDate)}</td>
        <td>#${transaction.accountId}</td>
        <td>${BankApp.escapeHtml(transaction.description)}</td>
        <td><span class="badge ${transaction.transactionType === "CREDIT" ? "badge-current" : "badge-user"}">${transaction.transactionType}</span></td>
        <td class="${transaction.transactionType === "CREDIT" ? "amount-credit" : "amount-debit"}">${transaction.transactionType === "CREDIT" ? "+" : "−"}${BankApp.currency(transaction.amount)}</td>
      </tr>`).join("")
    : '<tr><td colspan="6"><div class="empty-state">No transactions found.</div></td></tr>';
}

async function loadAdminPage() {
  const endpoint = adminSection === "customers" ? "/customer/fetch"
    : adminSection === "accounts" ? "/account/fetch"
      : "/transaction/fetch";
  BankApp.setLoading(true);
  try {
    const response = await BankApp.secureFetch(endpoint);
    if (!response.ok) {
      throw new Error("Unable to load " + adminSection);
    }
    const data = await response.json();
    if (adminSection === "customers") {
      renderCustomers(data);
    } else if (adminSection === "accounts") {
      renderAccounts(data);
    } else {
      renderTransactions(data);
    }
  } catch (error) {
    BankApp.showToast(error.message, "error");
  } finally {
    BankApp.setLoading(false);
  }
}

async function deleteCustomer(customerId) {
  if (!confirm("Delete this customer and all linked accounts and transactions?")) {
    return;
  }
  BankApp.setLoading(true);
  try {
    const response = await BankApp.secureFetch("/customer/delete/" + customerId, { method: "DELETE" });
    const message = BankApp.messageText(await BankApp.readResponse(response));
    if (!response.ok || !message.toLowerCase().includes("success")) {
      throw new Error(message);
    }
    BankApp.showToast(message, "success");
    await loadAdminPage();
  } catch (error) {
    BankApp.showToast(error.message, "error");
  } finally {
    BankApp.setLoading(false);
  }
}

if (pageAdmin) {
  setupAdminPage();
  loadAdminPage();
  if (adminSection === "customers") {
    document.getElementById("pageTable").addEventListener("click", event => {
      const button = event.target.closest("[data-delete-customer]");
      if (button) {
        deleteCustomer(button.dataset.deleteCustomer);
      }
    });
  }
}

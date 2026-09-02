/* ===================================================
   SMARTEXPENSEPRO - DEBITED-ONLY TRACKING & LOGIC ENGINE
   =================================================== */

// Initial Seed Transactions (Only Debited Expenses)
const INITIAL_TRANSACTIONS = [
  {
    id: "tx-1",
    type: "Debit",
    amount: 450.00,
    category: "Food",
    note: "Swiggy - Gourmet Pizza",
    date: "2026-09-02",
    paymentMethod: "UPI"
  },
  {
    id: "tx-2",
    type: "Debit",
    amount: 1299.00,
    category: "Shopping",
    note: "Amazon - Wireless Earbuds",
    date: "2026-09-02",
    paymentMethod: "Card"
  },
  {
    id: "tx-3",
    type: "Debit",
    amount: 380.00,
    category: "Travel",
    note: "Uber Premier Ride",
    date: "2026-09-01",
    paymentMethod: "UPI"
  },
  {
    id: "tx-4",
    type: "Debit",
    amount: 2400.00,
    category: "Bills",
    note: "Airtel Fiber & Electricity Bill",
    date: "2026-08-30",
    paymentMethod: "UPI"
  },
  {
    id: "tx-5",
    type: "Debit",
    amount: 850.00,
    category: "Entertainment",
    note: "PVR Cinemas IMAX Movie Tickets",
    date: "2026-08-29",
    paymentMethod: "Card"
  },
  {
    id: "tx-6",
    type: "Debit",
    amount: 620.00,
    category: "Health",
    note: "Apollo Pharmacy Medicine",
    date: "2026-08-28",
    paymentMethod: "UPI"
  },
  {
    id: "tx-7",
    type: "Debit",
    amount: 16151.00,
    category: "Others",
    note: "Home Maintenance & Repairs",
    date: "2026-08-27",
    paymentMethod: "NetBanking"
  }
];

const CATEGORY_ICONS = {
  Food: "🍔",
  Travel: "🚗",
  Bills: "💡",
  Shopping: "🛍️",
  Health: "💊",
  Entertainment: "🎬",
  Education: "📚",
  Others: "📦"
};

const CATEGORY_COLORS = {
  Food: "#FF6B6B",
  Travel: "#4ECDC4",
  Bills: "#45B7D1",
  Shopping: "#FFA07A",
  Health: "#98D8C8",
  Entertainment: "#FFD700",
  Education: "#9B59B6",
  Others: "#BDC3C7"
};

// Strict SMS Matching Rules Matching SmsParser.java
const DEBIT_KEYWORDS = [
  "debited", "spent", "withdrawn", "deducted",
  "purchase", "payment of", "paid to", "sent to",
  "transferred to", "charged"
];

const CREDIT_KEYWORDS = [
  "credited", "received", "deposited", "refund",
  "cashback", "credit of", "added to", "reversed"
];

// SMS Templates
const SMS_TEMPLATES = {
  hdfc: "HDFC Bank Alert: Rs. 450.00 debited from A/C XX4921 at SWIGGY BANGALORE via UPI on 02-SEP-26. Avl Bal: INR 42,850.00",
  sbi: "Dear SBI User, your A/C ending 8392 is debited for Rs 1299.00 on 02-Sep-26 towards AMAZON INDIA via Debit Card. Avl Bal: Rs 41,551.00",
  icici: "ICICI Bank: Acct XX9012 debited with INR 380.00 on 01-Sep-26 info: UBER TRIPS UPI/ref: 489218. Avail Bal: Rs 43,230.00",
  salary: "HDFC Bank: Rs 65,000.00 credited to your A/C XX4921 on 01-SEP-26 by TECH CORP HRMS (Salary for Aug 2026). Avail Bal: Rs 78,500.00"
};

class SmartExpenseApp {
  constructor() {
    this.user = JSON.parse(localStorage.getItem("sep_user")) || { name: "Sowndharya PL", email: "demo@smartexpense.pro", isLoggedIn: true };
    this.transactions = JSON.parse(localStorage.getItem("sep_txs")) || INITIAL_TRANSACTIONS;
    
    // Clean up any legacy income records
    this.transactions = this.transactions.filter(t => t.type !== "Income" && t.category !== "Salary" && t.category !== "Investment");
    
    this.monthlyBudget = 40000;
    this.currentFilter = "All";
    this.activeScreen = "screenDashboard";
    this.pieChartInstance = null;
    this.barChartInstance = null;

    this.init();
  }

  init() {
    this.setupClock();
    this.bindEvents();
    this.render();
    
    // Simulate splash screen on initial load
    setTimeout(() => {
      document.getElementById("screenSplash").classList.remove("active");
      if (this.user.isLoggedIn) {
        this.navigateTo("screenDashboard");
      } else {
        this.navigateTo("screenAuth");
      }
    }, 1200);

    // Set today's date in add modal
    document.getElementById("inputDate").value = new Date().toISOString().split("T")[0];
    lucide.createIcons();
  }

  setupClock() {
    const updateTime = () => {
      const now = new Date();
      const hrs = String(now.getHours()).padStart(2, '0');
      const mins = String(now.getMinutes()).padStart(2, '0');
      const el = document.getElementById("statusTime");
      if (el) el.textContent = `${hrs}:${mins}`;
    };
    updateTime();
    setInterval(updateTime, 10000);
  }

  bindEvents() {
    // Navigation & View Controls
    document.getElementById("btnToggleFrame").addEventListener("click", () => {
      document.getElementById("phoneContainer").classList.toggle("full-view");
      this.showToast("View mode toggled", "info");
    });

    document.getElementById("btnResetData").addEventListener("click", () => {
      this.transactions = [...INITIAL_TRANSACTIONS];
      this.saveState();
      this.render();
      this.showToast("Data reset (Debited only)", "success");
    });

    // Auth events
    document.getElementById("btnQuickDemo").addEventListener("click", () => {
      this.user.isLoggedIn = true;
      this.saveState();
      this.navigateTo("screenDashboard");
      this.showToast("Logged in as Demo User", "success");
    });

    document.getElementById("btnLogout").addEventListener("click", () => {
      this.user.isLoggedIn = false;
      this.saveState();
      this.navigateTo("screenAuth");
      this.showToast("Logged out", "info");
    });

    // Bottom Navigation Tabs
    document.getElementById("tabNavHome").addEventListener("click", () => this.navigateTo("screenDashboard"));
    document.getElementById("tabNavAnalytics").addEventListener("click", () => this.navigateTo("screenAnalytics"));
    document.getElementById("btnNavAnalytics").addEventListener("click", () => this.navigateTo("screenAnalytics"));
    document.getElementById("btnBackFromAnalytics").addEventListener("click", () => this.navigateTo("screenDashboard"));
    
    document.getElementById("tabNavSms").addEventListener("click", () => this.openModal("modalSmsSimulator"));
    document.getElementById("btnOpenSmsModal").addEventListener("click", () => this.openModal("modalSmsSimulator"));
    const smsActionBtn = document.getElementById("btnOpenSmsAction");
    if (smsActionBtn) smsActionBtn.addEventListener("click", () => this.openModal("modalSmsSimulator"));

    document.getElementById("tabNavExport").addEventListener("click", () => this.exportCsv());
    document.getElementById("btnQuickExport").addEventListener("click", () => this.exportCsv());

    // Add Expense Modal
    document.getElementById("fabAdd").addEventListener("click", () => this.openAddModal());
    document.getElementById("btnQuickAddExpense").addEventListener("click", () => this.openAddModal());

    document.getElementById("btnCloseAddModal").addEventListener("click", () => this.closeModal("modalAddTransaction"));
    document.getElementById("closeAddModalBackdrop").addEventListener("click", () => this.closeModal("modalAddTransaction"));

    // Add Modal Form Submit
    document.getElementById("formAddTransaction").addEventListener("submit", (e) => {
      e.preventDefault();
      this.handleAddExpense();
    });

    // Payment Mode Chips
    document.querySelectorAll(".pay-chip").forEach(chip => {
      chip.addEventListener("click", () => {
        document.querySelectorAll(".pay-chip").forEach(c => c.classList.remove("active"));
        chip.classList.add("active");
      });
    });

    // Category Filter Pills
    document.querySelectorAll(".cat-pill").forEach(pill => {
      pill.addEventListener("click", () => {
        document.querySelectorAll(".cat-pill").forEach(p => p.classList.remove("active"));
        pill.classList.add("active");
        this.currentFilter = pill.dataset.filter;
        this.renderTransactions();
      });
    });

    // SMS Modal Events
    document.getElementById("btnCloseSmsModal").addEventListener("click", () => this.closeModal("modalSmsSimulator"));
    document.getElementById("closeSmsModalBackdrop").addEventListener("click", () => this.closeModal("modalSmsSimulator"));

    document.querySelectorAll(".sms-preset-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        const key = btn.dataset.template;
        const text = SMS_TEMPLATES[key] || "";
        document.getElementById("txtSmsBody").value = text;
        this.previewSmsParse(text);
      });
    });

    document.getElementById("btnTestParseOnly").addEventListener("click", () => {
      const text = document.getElementById("txtSmsBody").value;
      this.previewSmsParse(text);
    });

    document.getElementById("btnParseAndSave").addEventListener("click", () => {
      const text = document.getElementById("txtSmsBody").value;
      this.handleSmsAutoSave(text);
    });

    // Sidebar Shortcuts
    document.getElementById("sideBtnAddRandom").addEventListener("click", () => this.addRandomExpense());
    document.getElementById("sideBtnSimulateSms").addEventListener("click", () => {
      const text = SMS_TEMPLATES.hdfc;
      this.handleSmsAutoSave(text);
    });
    document.getElementById("sideBtnExport").addEventListener("click", () => this.exportCsv());
  }

  navigateTo(screenId) {
    document.querySelectorAll(".screen").forEach(s => s.classList.remove("active"));
    const target = document.getElementById(screenId);
    if (target) {
      target.classList.add("active");
      this.activeScreen = screenId;
    }

    // Update bottom nav highlights
    document.querySelectorAll(".mobile-bottom-nav .nav-item").forEach(item => item.classList.remove("active"));
    if (screenId === "screenDashboard") {
      document.getElementById("tabNavHome").classList.add("active");
    } else if (screenId === "screenAnalytics") {
      document.getElementById("tabNavAnalytics").classList.add("active");
      this.renderAnalyticsCharts();
    }
  }

  openModal(modalId) {
    document.getElementById(modalId).classList.add("active");
    lucide.createIcons();
  }

  closeModal(modalId) {
    document.getElementById(modalId).classList.remove("active");
  }

  openAddModal() {
    this.openModal("modalAddTransaction");
    document.getElementById("selectCategory").value = "Food";
    document.getElementById("inputAmount").value = "";
    document.getElementById("inputNote").value = "";
  }

  handleAddExpense() {
    const amount = parseFloat(document.getElementById("inputAmount").value);
    const category = document.getElementById("selectCategory").value;
    const note = document.getElementById("inputNote").value.trim();
    const date = document.getElementById("inputDate").value;
    const paymentMethod = document.querySelector(".pay-chip.active").dataset.mode;

    if (isNaN(amount) || amount <= 0) {
      this.showToast("Please enter a valid amount", "info");
      return;
    }

    const newTx = {
      id: "tx-" + Date.now(),
      type: "Debit",
      amount,
      category,
      note: note || category,
      date: date || new Date().toISOString().split("T")[0],
      paymentMethod
    };

    this.transactions.unshift(newTx);
    this.saveState();
    this.closeModal("modalAddTransaction");
    document.getElementById("formAddTransaction").reset();
    document.getElementById("inputDate").value = new Date().toISOString().split("T")[0];

    this.render();
    this.showToast(`Debited ₹${amount.toFixed(2)} added!`, "success");
  }

  // Exact matching algorithm with SmsParser.java
  parseSms(sms) {
    if (!sms || sms.trim() === "") return null;
    const lower = sms.toLowerCase();

    // Step 1: Immediately reject if it contains any CREDIT keywords
    for (const creditWord of CREDIT_KEYWORDS) {
      if (lower.contains ? lower.contains(creditWord) : lower.indexOf(creditWord) !== -1) {
        return {
          isDebit: false,
          isCredit: true,
          reason: `Credit keyword "${creditWord}" detected. Ignored.`
        };
      }
    }

    // Step 2: Accept if it has debit keyword or is a valid spending message
    let isDebit = false;
    for (const debitWord of DEBIT_KEYWORDS) {
      if (lower.indexOf(debitWord) !== -1) {
        isDebit = true;
        break;
      }
    }

    // Step 3: Extract Amount
    let amount = 0;
    const amtRegex = /(?:rs\.?|inr)\s?([0-9,]+(?:\.[0-9]{1,2})?)/i;
    const match = sms.match(amtRegex);
    if (match && match[1]) {
      amount = parseFloat(match[1].replace(/,/g, ''));
    } else {
      const fallbackMatch = sms.match(/([0-9]+(?:\.[0-9]{2}))/);
      if (fallbackMatch) amount = parseFloat(fallbackMatch[1]);
    }

    if (!isDebit && amount > 0 && (lower.includes("swiggy") || lower.includes("uber") || lower.includes("amazon"))) {
      isDebit = true;
    }

    if (!isDebit) {
      return {
        isDebit: false,
        isCredit: false,
        reason: "No debit transaction keyword found."
      };
    }

    // Step 4: Detect Category & Merchant
    let category = "Others";
    let merchant = "Bank Debit";

    if (lower.includes("swiggy") || lower.includes("zomato") || lower.includes("restaurant") || lower.includes("starbucks") || lower.includes("mcdonald") || lower.includes("pizza")) {
      category = "Food";
      merchant = lower.includes("swiggy") ? "Swiggy Food" : lower.includes("zomato") ? "Zomato" : "Restaurant";
    } else if (lower.includes("uber") || lower.includes("ola") || lower.includes("rapido") || lower.includes("petrol") || lower.includes("fuel")) {
      category = "Travel";
      merchant = lower.includes("uber") ? "Uber Premier" : lower.includes("ola") ? "Ola Cab" : "Fuel Station";
    } else if (lower.includes("amazon") || lower.includes("flipkart") || lower.includes("myntra") || lower.includes("shopping") || lower.includes("zara")) {
      category = "Shopping";
      merchant = lower.includes("amazon") ? "Amazon India" : lower.includes("flipkart") ? "Flipkart" : "Retail Store";
    } else if (lower.includes("airtel") || lower.includes("jio") || lower.includes("electricity") || lower.includes("bill") || lower.includes("bescom") || lower.includes("tneb")) {
      category = "Bills";
      merchant = "Utility Bill Payment";
    } else if (lower.includes("apollo") || lower.includes("pharmacy") || lower.includes("hospital") || lower.includes("medicine")) {
      category = "Health";
      merchant = "Pharmacy / Medical Care";
    } else if (lower.includes("pvr") || lower.includes("inox") || lower.includes("cinema") || lower.includes("netflix") || lower.includes("bookmyshow")) {
      category = "Entertainment";
      merchant = "Cinema / Entertainment";
    } else if (lower.includes("school") || lower.includes("college") || lower.includes("tuition") || lower.includes("udemy") || lower.includes("coursera")) {
      category = "Education";
      merchant = "Education / Course Fee";
    }

    return {
      isDebit: true,
      type: "Debit",
      amount: amount || 0,
      category,
      merchant,
      date: new Date().toISOString().split("T")[0],
      paymentMethod: lower.includes("upi") ? "UPI" : lower.includes("card") ? "Card" : "NetBanking"
    };
  }

  previewSmsParse(text) {
    const result = this.parseSms(text);
    const box = document.getElementById("parsedResultBox");
    const statusBadge = document.getElementById("parsedStatusBadge");
    const grid = document.getElementById("parsedGridContent");
    const rejectMsg = document.getElementById("parsedRejectMessage");

    box.style.display = "block";

    if (!result || !result.isDebit) {
      statusBadge.innerHTML = `<i data-lucide="alert-circle" style="color: #EF4444;"></i> <span style="color: #EF4444;">Credit / Non-Debit SMS (Ignored)</span>`;
      grid.style.display = "none";
      rejectMsg.style.display = "block";
      this.showToast("Ignored: Credit SMS is not calculated", "info");
    } else {
      statusBadge.innerHTML = `<i data-lucide="check-circle-2" style="color: #10B981;"></i> <span style="color: #10B981;">Valid Debited SMS Extracted</span>`;
      grid.style.display = "grid";
      rejectMsg.style.display = "none";

      document.getElementById("parsedAmt").textContent = `₹ ${result.amount.toFixed(2)}`;
      document.getElementById("parsedType").textContent = "Debited Expense";
      document.getElementById("parsedCat").textContent = `${CATEGORY_ICONS[result.category] || ""} ${result.category}`;
      document.getElementById("parsedMerchant").textContent = result.merchant;
    }

    lucide.createIcons();
  }

  handleSmsAutoSave(text) {
    const result = this.parseSms(text);
    if (!result || !result.isDebit || result.amount <= 0) {
      this.showToast("Credit SMS rejected! Only debited expenses are calculated.", "info");
      return;
    }

    const newTx = {
      id: "tx-" + Date.now(),
      type: "Debit",
      amount: result.amount,
      category: result.category,
      note: result.merchant,
      date: result.date,
      paymentMethod: result.paymentMethod
    };

    this.transactions.unshift(newTx);
    this.saveState();
    this.closeModal("modalSmsSimulator");
    this.render();
    this.showToast(`Auto-Logged Debit: ₹${result.amount.toFixed(2)} at ${result.merchant}`, "success");
  }

  addRandomExpense() {
    const samples = [
      { amount: 180, category: "Food", note: "Starbucks Coffee", mode: "UPI" },
      { amount: 240, category: "Travel", note: "Auto Rickshaw Fare", mode: "Cash" },
      { amount: 799, category: "Shopping", note: "Zara Cotton Tee", mode: "Card" },
      { amount: 499, category: "Entertainment", note: "Netflix Monthly HD", mode: "UPI" },
      { amount: 350, category: "Health", note: "Vitamins & Supplements", mode: "UPI" }
    ];
    const s = samples[Math.floor(Math.random() * samples.length)];
    const newTx = {
      id: "tx-" + Date.now(),
      type: "Debit",
      amount: s.amount,
      category: s.category,
      note: s.note,
      date: new Date().toISOString().split("T")[0],
      paymentMethod: s.mode
    };
    this.transactions.unshift(newTx);
    this.saveState();
    this.render();
    this.showToast(`Debited ₹${s.amount} (${s.note})`, "success");
  }

  deleteTransaction(id) {
    this.transactions = this.transactions.filter(t => t.id !== id);
    this.saveState();
    this.render();
    this.showToast("Transaction deleted", "info");
  }

  exportCsv() {
    if (this.transactions.length === 0) {
      this.showToast("No debited expenses to export", "info");
      return;
    }

    let csvContent = "data:text/csv;charset=utf-8,";
    csvContent += "ID,Date,Type,Category,Debited Amount (INR),Payment Mode,Description\n";

    this.transactions.forEach(t => {
      const cleanNote = (t.note || "").replace(/,/g, " ");
      csvContent += `${t.id},${t.date},Debit,${t.category},${t.amount.toFixed(2)},${t.paymentMethod},"${cleanNote}"\n`;
    });

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `SmartExpense_Debits_${new Date().toISOString().split("T")[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    this.showToast("Exported SmartExpense_Debits.csv!", "success");
  }

  render() {
    // Strictly calculate total debited expenses
    let totalDebited = 0;
    this.transactions.forEach(t => {
      totalDebited += t.amount;
    });

    document.getElementById("displayNetBalance").textContent = `₹ ${totalDebited.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
    document.getElementById("displayTotalExpense").textContent = `₹ ${totalDebited.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
    document.getElementById("transactionCountBadge").textContent = `${this.transactions.length} records`;

    // Monthly Budget Widget
    const budgetPct = Math.min(Math.round((totalDebited / this.monthlyBudget) * 100), 100);
    const budgetProgress = document.getElementById("budgetProgress");
    const budgetPercentText = document.getElementById("budgetPercent");
    const budgetSubtitle = document.getElementById("budgetSubtitle");

    if (budgetSubtitle) {
      budgetSubtitle.textContent = `₹ ${totalDebited.toLocaleString('en-IN', { maximumFractionDigits: 0 })} debited of ₹ ${this.monthlyBudget.toLocaleString('en-IN')}`;
    }

    if (budgetProgress && budgetPercentText) {
      budgetProgress.style.width = `${budgetPct}%`;
      budgetPercentText.textContent = `${budgetPct}%`;
      if (budgetPct > 90) {
        budgetProgress.style.background = "linear-gradient(90deg, #F59E0B, #EF4444)";
      } else {
        budgetProgress.style.background = "linear-gradient(90deg, #6366F1, #EC4899)";
      }
    }

    this.renderTransactions();
    if (this.activeScreen === "screenAnalytics") {
      this.renderAnalyticsCharts();
    }
  }

  renderTransactions() {
    const container = document.getElementById("transactionListContainer");
    const emptyState = document.getElementById("emptyState");
    container.innerHTML = "";

    const filtered = this.transactions.filter(t => {
      if (this.currentFilter === "All") return true;
      return t.category.toLowerCase() === this.currentFilter.toLowerCase();
    });

    if (filtered.length === 0) {
      emptyState.style.display = "block";
      return;
    }
    emptyState.style.display = "none";

    filtered.forEach(tx => {
      const icon = CATEGORY_ICONS[tx.category] || "📦";
      const catColor = CATEGORY_COLORS[tx.category] || "#64748B";

      const item = document.createElement("div");
      item.className = "tx-item";
      item.innerHTML = `
        <div class="tx-left">
          <div class="tx-icon" style="background: ${catColor}20; color: ${catColor}; border: 1px solid ${catColor}40;">
            ${icon}
          </div>
          <div class="tx-details">
            <span class="tx-merchant">${this.escapeHtml(tx.note || tx.category)}</span>
            <div class="tx-meta">
              <span>${tx.date}</span>
              <span>•</span>
              <span class="tx-badge-pay">${tx.paymentMethod || 'UPI'}</span>
            </div>
          </div>
        </div>
        <div class="tx-right">
          <span class="tx-amount expense">
            - ₹${tx.amount.toFixed(2)}
          </span>
          <button class="btn-delete-tx" data-id="${tx.id}" title="Delete Transaction">
            <i data-lucide="trash-2"></i>
          </button>
        </div>
      `;

      item.querySelector(".btn-delete-tx").addEventListener("click", (e) => {
        e.stopPropagation();
        this.deleteTransaction(tx.id);
      });

      container.appendChild(item);
    });

    lucide.createIcons();
  }

  renderAnalyticsCharts() {
    let totalExpense = 0;
    const catTotals = {};

    this.transactions.forEach(t => {
      totalExpense += t.amount;
      catTotals[t.category] = (catTotals[t.category] || 0) + t.amount;
    });

    document.getElementById("analyticsTotalExpense").textContent = `₹ ${totalExpense.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;

    const categories = Object.keys(catTotals);
    const amounts = Object.values(catTotals);
    const bgColors = categories.map(c => CATEGORY_COLORS[c] || "#94A3B8");

    // Breakdown List
    const breakdownList = document.getElementById("categoryBreakdownList");
    breakdownList.innerHTML = "";
    categories.forEach(cat => {
      const amt = catTotals[cat];
      const pct = totalExpense > 0 ? ((amt / totalExpense) * 100).toFixed(1) : 0;
      const color = CATEGORY_COLORS[cat] || "#94A3B8";

      const row = document.createElement("div");
      row.className = "breakdown-row";
      row.innerHTML = `
        <div class="breakdown-cat-name">
          <span class="cat-indicator-dot" style="background: ${color};"></span>
          <span>${CATEGORY_ICONS[cat] || '📦'} ${cat}</span>
        </div>
        <div class="breakdown-values">
          <span class="breakdown-amount">₹ ${amt.toFixed(2)}</span>
          <span class="breakdown-share">${pct}% of total</span>
        </div>
      `;
      breakdownList.appendChild(row);
    });

    // Donut Chart
    const pieCanvas = document.getElementById("categoryPieChart");
    if (pieCanvas) {
      if (this.pieChartInstance) this.pieChartInstance.destroy();
      this.pieChartInstance = new Chart(pieCanvas, {
        type: 'doughnut',
        data: {
          labels: categories,
          datasets: [{
            data: amounts,
            backgroundColor: bgColors,
            borderColor: '#111827',
            borderWidth: 3,
            hoverOffset: 6
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'bottom',
              labels: {
                color: '#94A3B8',
                font: { family: 'Plus Jakarta Sans', size: 11 },
                padding: 10,
                boxWidth: 10
              }
            }
          },
          cutout: '62%'
        }
      });
    }

    // Weekly Bar Chart
    const barCanvas = document.getElementById("weeklyBarChart");
    if (barCanvas) {
      if (this.barChartInstance) this.barChartInstance.destroy();
      this.barChartInstance = new Chart(barCanvas, {
        type: 'bar',
        data: {
          labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4'],
          datasets: [{
            label: 'Debited (₹)',
            data: [4200, 7800, 3400, totalExpense > 15400 ? totalExpense - 15400 : 6750],
            backgroundColor: 'rgba(99, 102, 241, 0.8)',
            borderRadius: 6,
            hoverBackgroundColor: '#818CF8'
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          },
          scales: {
            x: {
              grid: { display: false },
              ticks: { color: '#64748B', font: { size: 10 } }
            },
            y: {
              grid: { color: 'rgba(255, 255, 255, 0.05)' },
              ticks: { color: '#64748B', font: { size: 10 } }
            }
          }
        }
      });
    }
  }

  saveState() {
    localStorage.setItem("sep_user", JSON.stringify(this.user));
    localStorage.setItem("sep_txs", JSON.stringify(this.transactions));
  }

  showToast(message, type = "success") {
    const container = document.getElementById("toastContainer");
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerHTML = `
      <i data-lucide="${type === 'success' ? 'check-circle' : 'info'}"></i>
      <span>${message}</span>
    `;
    container.appendChild(toast);
    lucide.createIcons();

    setTimeout(() => {
      toast.style.opacity = "0";
      toast.style.transform = "translateX(100%)";
      toast.style.transition = "all 0.3s ease";
      setTimeout(() => toast.remove(), 300);
    }, 3000);
  }

  escapeHtml(str) {
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
  }
}

// Start app on DOMContentLoaded
document.addEventListener("DOMContentLoaded", () => {
  window.sepApp = new SmartExpenseApp();
});

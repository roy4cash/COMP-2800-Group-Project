// ── Supabase Config ───────────────────────────────────────────────────────────
const SUPABASE_URL      = 'https://wmrkmjdzgbtojpkmmzkr.supabase.co';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndtcmttamR6Z2J0b2pwa21temtyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ3MzczNDgsImV4cCI6MjA5MDMxMzM0OH0.bPZf4ZjFPApxG8EZpYRXZLdFFOlqhJbzTHNcQ4exDik';

const { createClient } = supabase;
const db = createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

const USER_ID = 1;
let chart = null;

// ── Startup ───────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    setMonthLabel();
    setDefaultDate();
    loadCategories();
    refresh();
});

document.getElementById('expenseForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    await handleAddExpense();
});

// ── Helpers ───────────────────────────────────────────────────────────────────
function currentMonthRange() {
    const now   = new Date();
    const month = now.getMonth() + 1;
    const year  = now.getFullYear();
    const start = `${year}-${String(month).padStart(2, '0')}-01`;
    const end   = new Date(year, month, 0).toISOString().split('T')[0];
    return { month, year, start, end };
}

function formatCurrency(n) {
    return '$' + Math.abs(n).toFixed(2);
}

function formatDate(str) {
    return new Date(str + 'T00:00:00').toLocaleDateString('en-CA', {
        month: 'short', day: 'numeric', year: 'numeric'
    });
}

function setDefaultDate() {
    document.getElementById('expenseDate').value = new Date().toISOString().split('T')[0];
}

function setMonthLabel() {
    const label = new Date().toLocaleDateString('en-CA', { month: 'long', year: 'numeric' });
    document.getElementById('monthLabel').textContent = label;
}

function showFormError(msg) {
    const el = document.getElementById('formError');
    el.textContent = msg;
    el.className = 'msg error';
}

// ── Refresh all panels ────────────────────────────────────────────────────────
async function refresh() {
    await Promise.all([loadSummary(), loadExpenses(), loadChart()]);
}

// ── Load Categories ───────────────────────────────────────────────────────────
async function loadCategories() {
    const { data } = await db.from('categories').select('*').order('name');
    const select = document.getElementById('categorySelect');
    select.innerHTML = data.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}

// ── Summary Cards + Progress + Alert ─────────────────────────────────────────
async function loadSummary() {
    const { month, year, start, end } = currentMonthRange();

    const [budgetRes, expenseRes] = await Promise.all([
        db.from('budgets').select('amount').eq('user_id', USER_ID).eq('month', month).eq('year', year).single(),
        db.from('expenses').select('amount').eq('user_id', USER_ID).gte('expense_date', start).lte('expense_date', end)
    ]);

    const budget    = budgetRes.data?.amount ?? 0;
    const spent     = (expenseRes.data ?? []).reduce((sum, e) => sum + parseFloat(e.amount), 0);
    const remaining = budget - spent;
    const ratio     = budget > 0 ? spent / budget : 0;

    // Cards
    document.getElementById('budgetAmount').textContent    = formatCurrency(budget);
    document.getElementById('spentAmount').textContent     = formatCurrency(spent);
    document.getElementById('remainingAmount').textContent = (remaining < 0 ? '-' : '') + formatCurrency(remaining);

    const remainingEl = document.getElementById('remainingAmount');
    remainingEl.className = 'card-value ' + (remaining < 0 ? 'danger' : 'success');

    // Progress bar
    const pct  = Math.min(ratio * 100, 100);
    const fill = document.getElementById('progressFill');
    fill.style.width      = pct + '%';
    fill.style.background = ratio >= 1 ? 'var(--danger)' : ratio >= 0.8 ? 'var(--warning)' : 'var(--success)';
    document.getElementById('progressLabel').textContent = `${Math.round(pct)}% of budget used`;

    // Alert banner
    const banner = document.getElementById('alertBanner');
    banner.className = 'alert-banner';

    if (budget <= 0) {
        banner.classList.add('hidden');
    } else if (ratio >= 1) {
        banner.classList.add('alert-danger');
        banner.textContent = `⚠️  Over budget! You have exceeded your limit by ${formatCurrency(Math.abs(remaining))}.`;
    } else if (ratio >= 0.8) {
        banner.classList.add('alert-warning');
        banner.textContent = `⚠️  Warning: You have used ${Math.round(ratio * 100)}% of your monthly budget.`;
    } else {
        banner.classList.add('alert-ok');
        banner.textContent = `✓  On track — ${Math.round(ratio * 100)}% of your budget used.`;
    }

    // Pre-fill budget input
    if (budget > 0) document.getElementById('budgetInput').value = budget;
}

// ── Transaction Table ─────────────────────────────────────────────────────────
async function loadExpenses() {
    const { data } = await db
        .from('expenses')
        .select('*, categories(name)')
        .eq('user_id', USER_ID)
        .order('expense_date', { ascending: false });

    const tbody = document.getElementById('expenseTableBody');

    if (!data || data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="no-data" style="padding:24px;text-align:center">No expenses recorded yet.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(e => `
        <tr>
            <td>${formatDate(e.expense_date)}</td>
            <td><span class="badge">${e.categories?.name ?? '—'}</span></td>
            <td>${e.description ?? '—'}</td>
            <td class="amount-cell">${formatCurrency(e.amount)}</td>
            <td><button class="btn-delete" onclick="deleteExpense(${e.id})" title="Delete">✕</button></td>
        </tr>
    `).join('');
}

// ── Pie Chart ─────────────────────────────────────────────────────────────────
async function loadChart() {
    const { start, end } = currentMonthRange();

    const { data } = await db
        .from('expenses')
        .select('amount, categories(name)')
        .eq('user_id', USER_ID)
        .gte('expense_date', start)
        .lte('expense_date', end);

    const noData = document.getElementById('noChartData');
    const canvas = document.getElementById('spendingChart');

    if (!data || data.length === 0) {
        noData.classList.remove('hidden');
        canvas.classList.add('hidden');
        if (chart) { chart.destroy(); chart = null; }
        return;
    }

    noData.classList.add('hidden');
    canvas.classList.remove('hidden');

    // Aggregate totals per category
    const totals = {};
    data.forEach(e => {
        const name = e.categories?.name ?? 'Other';
        totals[name] = (totals[name] ?? 0) + parseFloat(e.amount);
    });

    const labels = Object.keys(totals);
    const values = Object.values(totals);
    const palette = ['#4F46E5','#10B981','#F59E0B','#EF4444','#8B5CF6','#06B6D4','#F97316','#EC4899'];

    if (chart) chart.destroy();

    chart = new Chart(canvas, {
        type: 'doughnut',
        data: {
            labels,
            datasets: [{
                data: values,
                backgroundColor: palette.slice(0, labels.length),
                borderWidth: 3,
                borderColor: '#fff',
                hoverOffset: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '62%',
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { font: { size: 11, family: 'Inter' }, padding: 16, usePointStyle: true }
                },
                tooltip: {
                    callbacks: {
                        label: ctx => ` ${ctx.label}: $${ctx.parsed.toFixed(2)}`
                    }
                }
            }
        }
    });
}

// ── Add Expense ───────────────────────────────────────────────────────────────
async function handleAddExpense() {
    const description = document.getElementById('description').value.trim();
    const amount      = parseFloat(document.getElementById('amount').value);
    const date        = document.getElementById('expenseDate').value;
    const categoryId  = parseInt(document.getElementById('categorySelect').value);

    if (!description)      return showFormError('Description is required.');
    if (!amount || amount <= 0) return showFormError('Enter a valid positive amount.');
    if (!date)             return showFormError('Please select a date.');

    document.getElementById('formError').classList.add('hidden');

    await db.from('expenses').insert({
        user_id: USER_ID, category_id: categoryId,
        description, amount, expense_date: date
    });

    document.getElementById('expenseForm').reset();
    setDefaultDate();
    await refresh();
}

// ── Delete Expense ────────────────────────────────────────────────────────────
async function deleteExpense(id) {
    if (!confirm('Delete this expense?')) return;
    await db.from('expenses').delete().eq('id', id);
    await refresh();
}

// ── Set Budget ────────────────────────────────────────────────────────────────
async function setBudget() {
    const amount = parseFloat(document.getElementById('budgetInput').value);
    const msgEl  = document.getElementById('budgetMsg');

    if (!amount || amount <= 0) {
        msgEl.className = 'msg error';
        msgEl.textContent = 'Please enter a valid amount.';
        msgEl.classList.remove('hidden');
        return;
    }

    const { month, year } = currentMonthRange();

    await db.from('budgets').upsert(
        { user_id: USER_ID, month, year, amount },
        { onConflict: 'user_id,month,year' }
    );

    msgEl.className = 'msg success';
    msgEl.textContent = '✓ Budget updated!';
    msgEl.classList.remove('hidden');
    setTimeout(() => msgEl.classList.add('hidden'), 3000);

    await refresh();
}

const state = {
  products: [],
  customers: [],
  orders: [],
  customerId: null,
  category: "ALL",
  orderFilter: "ALL",
  editingCustomerId: null,
  editingProductId: null,
  editingOrderId: null,
  editLines: [],
  cart: new Map(),
  view: "catalog"
};

const CATEGORIES = {
  ALL: "Все",
  SKINCARE: "Уход",
  MAKEUP: "Макияж",
  HAIRCARE: "Волосы",
  FRAGRANCE: "Парфюмерия"
};

const PRODUCT_STATUS = {
  ACTIVE: "В наличии",
  OUT_OF_STOCK: "Нет в наличии",
  ARCHIVED: "Снят с продажи"
};

const ORDER_STATUS = {
  NEW: "Новый",
  PAID: "Оплачен",
  CANCELLED: "Отменён",
  COMPLETED: "Завершён"
};

const CUSTOMER_STATUS = {
  ACTIVE: "Активен",
  BLOCKED: "Заблокирован"
};

const REASONS = {
  CUSTOMER_BLOCKED: "Клиент заблокирован",
  PRODUCT_ARCHIVED: "Товар снят с продажи",
  PRODUCT_OUT_OF_STOCK: "Товара нет в наличии",
  INSUFFICIENT_STOCK: "Недостаточно товара на складе"
};

const grid = document.querySelector("#product-grid");
const filters = document.querySelector("#filters");
const customerSelect = document.querySelector("#customer-select");
const cartCount = document.querySelector("#cart-count");
const cartBody = document.querySelector("#cart-body");
const cartTotal = document.querySelector("#cart-total");
const cartNotice = document.querySelector("#cart-notice");
const ordersList = document.querySelector("#orders-list");
const ordersTitle = document.querySelector("#orders-title");
const customerForm = document.querySelector("#customer-form");
const productForm = document.querySelector("#product-form");
const orderForm = document.querySelector("#order-form");
const orderLines = document.querySelector("#order-lines");
const productAdminList = document.querySelector("#product-admin-list");
const customerAdminList = document.querySelector("#customer-admin-list");
const orderAdminList = document.querySelector("#order-admin-list");
const orderFilters = document.querySelector("#order-filters");
const analyticsBody = document.querySelector("#analytics-body");
const toast = document.querySelector("#toast");

document.querySelectorAll("[data-nav]").forEach((button) => {
  button.addEventListener("click", () => showView(button.dataset.nav));
});
document.querySelector("#open-cart").addEventListener("click", openCart);
document.querySelector("#close-cart").addEventListener("click", closeCart);
document.querySelector("#overlay").addEventListener("click", closeCart);
document.querySelector("#check-order").addEventListener("click", checkOrder);
document.querySelector("#create-order").addEventListener("click", createOrder);
customerSelect.addEventListener("change", () => {
  state.customerId = Number(customerSelect.value);
  clearNotice();
  if (state.view === "orders") {
    loadOrders();
  }
});
document.querySelector("#new-customer").addEventListener("click", () => openCustomerForm(null));
document.querySelector("#close-customer-form").addEventListener("click", closeCustomerForm);
customerForm.addEventListener("submit", saveCustomer);
document.querySelector("#new-product").addEventListener("click", () => openProductForm(null));
document.querySelector("#close-product-form").addEventListener("click", closeProductForm);
productForm.addEventListener("submit", saveProduct);
document.querySelector("#close-order-form").addEventListener("click", closeOrderForm);
document.querySelector("#add-order-line").addEventListener("click", () => {
  state.editLines.push({ productId: editableProducts()[0]?.id ?? "", quantity: 1 });
  renderOrderLines();
});
orderForm.addEventListener("submit", saveOrder);

init();

async function init() {
  renderFilters();
  try {
    const [products, customers] = await Promise.all([
      api("/api/v1/products"),
      api("/api/v1/customers")
    ]);
    state.products = products;
    state.customers = customers;
    state.customerId = customers[0]?.id ?? null;
    renderCustomers();
    renderProducts();
  } catch (error) {
    grid.innerHTML = `<div class="empty">${escapeHtml(error.message)}</div>`;
  }
}

function renderFilters() {
  filters.innerHTML = Object.entries(CATEGORIES).map(([key, label]) => `
    <button class="filter ${key === state.category ? "is-active" : ""}" type="button" data-category="${key}">${label}</button>
  `).join("");
  filters.querySelectorAll("[data-category]").forEach((button) => {
    button.addEventListener("click", () => {
      state.category = button.dataset.category;
      renderFilters();
      renderProducts();
    });
  });
}

function renderCustomers() {
  customerSelect.innerHTML = state.customers.map((customer) => `
    <option value="${customer.id}">${escapeHtml(customerLabel(customer))}</option>
  `).join("");
  customerSelect.value = String(state.customerId);
}

function renderProducts() {
  const visible = state.products.filter((product) =>
    product.status === "ACTIVE"
    && product.stockQuantity > 0
    && (state.category === "ALL" || product.category === state.category)
  );
  if (visible.length === 0) {
    grid.innerHTML = `<div class="empty">Нет товаров, доступных к заказу.</div>`;
    return;
  }
  grid.innerHTML = visible.map((product) => `
      <article class="card">
        <div class="visual ${product.category}">
          <img src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}" loading="lazy">
          <span>${escapeHtml(product.brand)}</span>
        </div>
        <div class="card-body">
          <div class="meta">${CATEGORIES[product.category]} · ${escapeHtml(product.sku)}</div>
          <h3>${escapeHtml(product.name)}</h3>
          <div class="price-row">
            <span class="price">${money(product.price)}</span>
            <span class="badge ${product.status}">${PRODUCT_STATUS[product.status]}</span>
          </div>
          <div class="meta">На складе: ${product.stockQuantity}</div>
          <button class="add-button" type="button" data-add="${product.id}">В корзину</button>
        </div>
      </article>
    `).join("");
  grid.querySelectorAll("[data-add]").forEach((button) => {
    button.addEventListener("click", () => addToCart(Number(button.dataset.add)));
  });
}

function addToCart(productId) {
  const product = state.products.find((item) => item.id === productId);
  if (!product || product.status !== "ACTIVE") {
    return;
  }
  const current = state.cart.get(productId) ?? 0;
  const next = Math.min(current + 1, product.stockQuantity, 20);
  if (next === current) {
    showToast("Больше добавить нельзя");
    return;
  }
  state.cart.set(productId, next);
  renderCart();
  openCart();
}

function renderCart() {
  const items = cartItems();
  cartCount.textContent = String(items.reduce((sum, item) => sum + item.quantity, 0));
  cartTotal.textContent = money(items.reduce((sum, item) => sum + Number(item.product.price) * item.quantity, 0));
  if (items.length === 0) {
    cartBody.innerHTML = `<div class="empty">Корзина пуста. Выберите что-нибудь из каталога.</div>`;
    return;
  }
  cartBody.innerHTML = items.map(({ product, quantity }) => `
    <div class="cart-item">
      <div>
        <strong>${escapeHtml(product.name)}</strong>
        <p>${escapeHtml(product.brand)} · ${money(product.price)}</p>
      </div>
      <button class="remove" type="button" data-remove="${product.id}">Удалить</button>
      <div class="qty">
        <button type="button" data-dec="${product.id}" aria-label="Уменьшить">−</button>
        <span>${quantity}</span>
        <button type="button" data-inc="${product.id}" aria-label="Увеличить">+</button>
      </div>
      <strong>${money(Number(product.price) * quantity)}</strong>
    </div>
  `).join("");
  cartBody.querySelectorAll("[data-remove]").forEach((button) => {
    button.addEventListener("click", () => {
      state.cart.delete(Number(button.dataset.remove));
      clearNotice();
      renderCart();
    });
  });
  cartBody.querySelectorAll("[data-dec]").forEach((button) => {
    button.addEventListener("click", () => changeQuantity(Number(button.dataset.dec), -1));
  });
  cartBody.querySelectorAll("[data-inc]").forEach((button) => {
    button.addEventListener("click", () => changeQuantity(Number(button.dataset.inc), 1));
  });
}

function changeQuantity(productId, delta) {
  const product = state.products.find((item) => item.id === productId);
  const next = (state.cart.get(productId) ?? 0) + delta;
  if (next <= 0) {
    state.cart.delete(productId);
  } else {
    state.cart.set(productId, Math.min(next, product.stockQuantity, 20));
  }
  clearNotice();
  renderCart();
}

function cartItems() {
  return [...state.cart.entries()]
    .map(([productId, quantity]) => ({
      product: state.products.find((item) => item.id === productId),
      quantity
    }))
    .filter((item) => item.product);
}

function orderPayload() {
  return {
    customerId: state.customerId,
    items: cartItems().map(({ product, quantity }) => ({ productId: product.id, quantity }))
  };
}

async function checkOrder() {
  if (!ensureCart()) {
    return;
  }
  try {
    const result = await api("/api/v1/orders/check", {
      method: "POST",
      body: JSON.stringify(orderPayload())
    });
    if (result.allowed) {
      showNotice(`Заказ можно оформить на ${money(result.totalAmount)}`, "good");
    } else {
      showNotice(REASONS[result.reason] || result.reason, "bad");
    }
  } catch (error) {
    showNotice(error.message, "bad");
  }
}

async function createOrder() {
  if (!ensureCart()) {
    return;
  }
  try {
    const order = await api("/api/v1/orders", {
      method: "POST",
      body: JSON.stringify(orderPayload())
    });
    state.cart.clear();
    clearNotice();
    renderCart();
    closeCart();
    showToast(`Заказ №${order.id} оформлен`);
    state.products = await api("/api/v1/products");
    renderProducts();
    showView("orders");
  } catch (error) {
    showNotice(error.message, "bad");
  }
}

async function loadOrders() {
  const customer = selectedCustomer();
  ordersTitle.textContent = customer ? `Заказы · ${customer.firstName} ${customer.lastName}` : "Заказы";
  if (!state.customerId) {
    ordersList.innerHTML = `<div class="empty">Клиенты не найдены.</div>`;
    return;
  }
  try {
    const orders = await api(`/api/v1/customers/${state.customerId}/orders`);
    if (orders.length === 0) {
      ordersList.innerHTML = `<div class="empty">У этого клиента пока нет заказов.</div>`;
      return;
    }
    const details = await Promise.all(orders.map((order) => api(`/api/v1/orders/${order.id}`)));
    state.orders = details;
    ordersList.innerHTML = details.map(renderOrder).join("");
    ordersList.querySelectorAll("[data-cancel]").forEach((button) => {
      button.addEventListener("click", () => cancelOrder(Number(button.dataset.cancel)));
    });
    ordersList.querySelectorAll("[data-edit-order]").forEach((button) => {
      button.addEventListener("click", () => openOrderForm(Number(button.dataset.editOrder)));
    });
  } catch (error) {
    ordersList.innerHTML = `<div class="empty">${escapeHtml(error.message)}</div>`;
  }
}

function renderOrder(order) {
  const items = order.items.map((item) => `
    <div class="line">
      <span>${escapeHtml(item.productName)} × ${item.quantity}</span>
      <span>${money(item.lineAmount)}</span>
    </div>
  `).join("");
  const actions = [];
  if (order.status === "NEW") {
    actions.push(`<button class="button button-ghost" type="button" data-edit-order="${order.id}">Изменить</button>`);
    actions.push(`<button class="button button-ghost" type="button" data-cancel="${order.id}">Отменить</button>`);
  }
  const actionsHtml = actions.length ? `<div class="order-actions">${actions.join("")}</div>` : "";
  return `
    <article class="order">
      <div class="order-top">
        <div>
          <strong>Заказ №${order.id}</strong>
          <div class="meta">${formatDate(order.createdAt)}</div>
        </div>
        <span class="badge ${order.status}">${ORDER_STATUS[order.status]}</span>
      </div>
      <div class="order-items">${items}</div>
      <div class="price-row"><span class="meta">Итого</span><strong>${money(order.totalAmount)}</strong></div>
      ${actionsHtml}
    </article>
  `;
}

async function cancelOrder(orderId) {
  try {
    await api(`/api/v1/orders/${orderId}/cancel`, { method: "POST" });
    showToast(`Заказ №${orderId} отменён`);
    state.products = await api("/api/v1/products");
    renderProducts();
    await loadOrders();
  } catch (error) {
    showToast(error.message);
  }
}

function showView(view) {
  closeOrderForm();
  state.view = ["orders", "admin", "analytics"].includes(view) ? view : "catalog";
  document.querySelector("#catalog-view").classList.toggle("hidden", state.view !== "catalog");
  document.querySelector("#orders-view").classList.toggle("hidden", state.view !== "orders");
  document.querySelector("#admin-view").classList.toggle("hidden", state.view !== "admin");
  document.querySelector("#analytics-view").classList.toggle("hidden", state.view !== "analytics");
  document.querySelectorAll(".nav-link").forEach((button) => {
    button.classList.toggle("is-active", button.dataset.nav === state.view);
  });
  if (state.view === "orders") {
    loadOrders();
  }
  if (state.view === "admin") {
    loadAdmin();
  }
  if (state.view === "analytics") {
    loadAnalytics();
  }
}

async function loadAnalytics() {
  analyticsBody.innerHTML = `<div class="empty">Считаю заказы…</div>`;
  try {
    const [orders, customers] = await Promise.all([
      api("/api/v1/orders"),
      api("/api/v1/customers")
    ]);
    state.orders = orders;
    state.customers = customers;
    renderAnalytics(orders, customers);
  } catch (error) {
    analyticsBody.innerHTML = `<div class="empty">${escapeHtml(error.message)}</div>`;
  }
}

function renderAnalytics(orders, customers) {
  if (orders.length === 0) {
    analyticsBody.innerHTML = `<div class="empty">Заказов пока нет.</div>`;
    return;
  }
  const byStatus = { NEW: emptyBucket(), PAID: emptyBucket(), COMPLETED: emptyBucket(), CANCELLED: emptyBucket() };
  for (const order of orders) {
    byStatus[order.status].count += 1;
    byStatus[order.status].amount += Number(order.totalAmount);
  }
  const revenueOrders = byStatus.PAID.count + byStatus.COMPLETED.count;
  const revenue = byStatus.PAID.amount + byStatus.COMPLETED.amount;
  const average = revenueOrders === 0 ? 0 : revenue / revenueOrders;
  const cancelShare = byStatus.CANCELLED.count / orders.length * 100;
  const selling = orders.filter((order) => order.status !== "CANCELLED");
  const quantity = selling.reduce((sum, order) => sum + order.items.reduce((lineSum, item) => lineSum + item.quantity, 0), 0);

  analyticsBody.innerHTML = `
    <div class="report">
      <div class="report-lead">
        <div>
          <p class="report-kicker">Общее количество</p>
          <p class="report-figure">${formatCount(quantity)}</p>
          <p class="report-note">товаров в заказах, без отменённых</p>
        </div>
        <dl class="report-facts">
          <div><dt>Заказы</dt><dd>${formatCount(orders.length)}</dd></div>
          <div><dt>Выручка</dt><dd>${money(revenue)}</dd></div>
          <div><dt>Средний чек</dt><dd>${money(average)}</dd></div>
          <div><dt>Отменено</dt><dd>${formatCount(byStatus.CANCELLED.count)} <small>${formatShare(cancelShare)}</small></dd></div>
        </dl>
      </div>
      <section class="report-block">
        <h2>Статусы</h2>
        <div class="mix" aria-hidden="true">${statusMix(byStatus)}</div>
        <ul class="legend">
          ${statusLegend(byStatus)}
        </ul>
      </section>
      <section class="report-block">
        <h2>Выручка по месяцам</h2>
        <div class="months">${monthColumns(orders)}</div>
      </section>
      <div class="report-split">
        <section class="report-block">
          <h2>Товары</h2>
          ${productList(selling)}
        </section>
        <section class="report-block">
          <h2>Клиенты</h2>
          ${customerList(selling, customers)}
        </section>
      </div>
    </div>
  `;
}

function emptyBucket() {
  return { count: 0, amount: 0 };
}

function statusMix(byStatus) {
  return ["NEW", "PAID", "COMPLETED", "CANCELLED"].map((status) => {
    const count = byStatus[status].count;
    return count ? `<i class="${status}" style="flex: ${count}"></i>` : "";
  }).join("");
}

function statusLegend(byStatus) {
  return ["NEW", "PAID", "COMPLETED", "CANCELLED"].map((status) => {
    const bucket = byStatus[status];
    return `
      <li>
        <span><i class="dot ${status}"></i>${ORDER_STATUS[status]}</span>
        <b>${formatCount(bucket.count)}</b>
        <em>${money(bucket.amount)}</em>
      </li>
    `;
  }).join("");
}

function monthColumns(orders) {
  const months = new Map();
  for (const order of orders) {
    const date = new Date(order.createdAt);
    const key = `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, "0")}`;
    const row = months.get(key) ?? { key, count: 0, revenue: 0 };
    row.count += 1;
    if (order.status === "PAID" || order.status === "COMPLETED") {
      row.revenue += Number(order.totalAmount);
    }
    months.set(key, row);
  }
  const rows = [...months.values()].sort((left, right) => left.key.localeCompare(right.key));
  const max = Math.max(...rows.map((row) => row.revenue), 1);
  return rows.map((row) => {
    const height = row.revenue === 0 ? 4 : Math.max(8, Math.round(row.revenue / max * 100));
    const title = `${monthLabel(row.key)} · ${formatCount(row.count)} заказов · ${money(row.revenue)}`;
    return `
      <div class="month" title="${escapeHtml(title)}">
        <div class="month-col"><span style="height: ${height}%"></span></div>
        <small>${monthShort(row.key)}</small>
      </div>
    `;
  }).join("");
}

function productList(orders) {
  const products = new Map();
  for (const order of orders) {
    for (const item of order.items) {
      const row = products.get(item.productId) ?? { name: item.productName, quantity: 0, amount: 0 };
      row.quantity += item.quantity;
      row.amount += Number(item.lineAmount);
      products.set(item.productId, row);
    }
  }
  const rows = [...products.values()].sort((left, right) => right.quantity - left.quantity || right.amount - left.amount).slice(0, 6);
  if (rows.length === 0) {
    return `<div class="empty">Пока нечего считать.</div>`;
  }
  return `<ol class="rank-list">${rows.map((row, index) => `
    <li>
      <span class="n">${index + 1}</span>
      <strong>${escapeHtml(row.name)}</strong>
      <em>${formatCount(row.quantity)} шт. · ${money(row.amount)}</em>
    </li>
  `).join("")}</ol>`;
}

function customerList(orders, customers) {
  const totals = new Map();
  for (const order of orders) {
    const row = totals.get(order.customerId) ?? { customerId: order.customerId, count: 0, amount: 0 };
    row.count += 1;
    row.amount += Number(order.totalAmount);
    totals.set(order.customerId, row);
  }
  const rows = [...totals.values()].sort((left, right) => right.amount - left.amount).slice(0, 6);
  return `<ol class="rank-list">${rows.map((row, index) => {
    const customer = customers.find((item) => item.id === row.customerId);
    const name = customer ? `${customer.firstName} ${customer.lastName}` : `Клиент ${row.customerId}`;
    return `
      <li>
        <span class="n">${index + 1}</span>
        <strong>${escapeHtml(name)}</strong>
        <em>${formatCount(row.count)} · ${money(row.amount)}</em>
      </li>
    `;
  }).join("")}</ol>`;
}

function monthShort(key) {
  const date = new Date(`${key}-01T00:00:00Z`);
  const month = new Intl.DateTimeFormat("ru-RU", { month: "short", timeZone: "UTC" }).format(date).replace(".", "");
  const year = String(date.getUTCFullYear()).slice(2);
  return `${month}<br>${year}`;
}

function monthLabel(key) {
  const label = new Intl.DateTimeFormat("ru-RU", { month: "long", year: "numeric", timeZone: "UTC" })
    .format(new Date(`${key}-01T00:00:00Z`));
  return label.charAt(0).toUpperCase() + label.slice(1);
}

function formatCount(value) {
  return new Intl.NumberFormat("ru-RU").format(value);
}

function formatShare(value) {
  return `${new Intl.NumberFormat("ru-RU", { maximumFractionDigits: 1 }).format(value)}%`;
}

async function loadAdmin() {
  renderOrderFilters();
  try {
    const [customers, orders, products] = await Promise.all([
      api("/api/v1/customers"),
      api("/api/v1/orders"),
      api("/api/v1/products")
    ]);
    state.customers = customers;
    state.orders = orders;
    state.products = products;
    if (!state.customers.some((customer) => customer.id === state.customerId)) {
      state.customerId = customers[0]?.id ?? null;
    }
    renderCustomers();
    renderCustomerAdmin();
    renderOrderAdmin();
    renderProductAdmin();
  } catch (error) {
    customerAdminList.innerHTML = `<div class="empty">${escapeHtml(error.message)}</div>`;
  }
}

function renderCustomerAdmin() {
  if (state.customers.length === 0) {
    customerAdminList.innerHTML = `<div class="empty">Клиентов пока нет.</div>`;
    return;
  }
  customerAdminList.innerHTML = state.customers.map((customer) => `
    <article class="person">
      <div class="person-top">
        <div>
          <strong>${escapeHtml(customer.firstName)} ${escapeHtml(customer.lastName)}</strong>
          <div class="meta">${escapeHtml(customer.phone)} · ${formatBirthDate(customer.birthDate)} · id ${customer.id}</div>
        </div>
        <span class="badge ${customer.status}">${CUSTOMER_STATUS[customer.status]}</span>
      </div>
      <div class="row-actions">
        <button class="button button-ghost button-small" type="button" data-edit="${customer.id}">Изменить</button>
        <button class="button button-ghost button-small" type="button" data-toggle="${customer.id}">
          ${customer.status === "ACTIVE" ? "Заблокировать" : "Разблокировать"}
        </button>
      </div>
    </article>
  `).join("");
  customerAdminList.querySelectorAll("[data-edit]").forEach((button) => {
    button.addEventListener("click", () => openCustomerForm(Number(button.dataset.edit)));
  });
  customerAdminList.querySelectorAll("[data-toggle]").forEach((button) => {
    button.addEventListener("click", () => toggleCustomer(Number(button.dataset.toggle)));
  });
}

function renderProductAdmin() {
  if (state.products.length === 0) {
    productAdminList.innerHTML = `<div class="empty">Товаров пока нет.</div>`;
    return;
  }
  productAdminList.innerHTML = state.products.map((product) => `
    <article class="admin-product">
      <img src="${escapeHtml(product.imageUrl)}" alt="">
      <div>
        <strong>${escapeHtml(product.name)}</strong>
        <div class="meta">${escapeHtml(product.brand)} · ${escapeHtml(product.sku)} · ${money(product.price)} · на складе ${product.stockQuantity}</div>
      </div>
      <div class="row-actions">
        <span class="badge ${product.status}">${PRODUCT_STATUS[product.status]}</span>
        <button class="button button-ghost button-small" type="button" data-edit-product="${product.id}">Изменить</button>
      </div>
    </article>
  `).join("");
  productAdminList.querySelectorAll("[data-edit-product]").forEach((button) => {
    button.addEventListener("click", () => openProductForm(Number(button.dataset.editProduct)));
  });
}

function openProductForm(productId) {
  state.editingProductId = productId;
  const product = state.products.find((item) => item.id === productId);
  document.querySelector("#product-sku").value = product?.sku ?? "";
  document.querySelector("#product-name").value = product?.name ?? "";
  document.querySelector("#product-brand").value = product?.brand ?? "";
  document.querySelector("#product-category").value = product?.category ?? "SKINCARE";
  document.querySelector("#product-price").value = product?.price ?? "";
  document.querySelector("#product-stock").value = product?.stockQuantity ?? "";
  document.querySelector("#product-status").value = product?.status ?? "ACTIVE";
  document.querySelector("#product-image").value = product?.imageUrl ?? "";
  productForm.classList.remove("hidden");
  productForm.scrollIntoView({ block: "nearest" });
}

function closeProductForm() {
  state.editingProductId = null;
  productForm.classList.add("hidden");
  productForm.reset();
}

async function saveProduct(event) {
  event.preventDefault();
  const body = {
    sku: document.querySelector("#product-sku").value.trim(),
    name: document.querySelector("#product-name").value.trim(),
    brand: document.querySelector("#product-brand").value.trim(),
    category: document.querySelector("#product-category").value,
    price: Number(document.querySelector("#product-price").value),
    stockQuantity: Number(document.querySelector("#product-stock").value),
    status: document.querySelector("#product-status").value,
    imageUrl: document.querySelector("#product-image").value.trim()
  };
  const editing = state.editingProductId;
  try {
    const saved = await api(editing ? `/api/v1/products/${editing}` : "/api/v1/products", {
      method: editing ? "PATCH" : "POST",
      body: JSON.stringify(body)
    });
    closeProductForm();
    showToast(editing ? "Товар обновлён" : `Товар №${saved.id} создан`);
    state.products = await api("/api/v1/products");
    renderProducts();
    renderProductAdmin();
  } catch (error) {
    showToast(error.message);
  }
}

function openCustomerForm(customerId) {
  state.editingCustomerId = customerId;
  const customer = state.customers.find((item) => item.id === customerId);
  document.querySelector("#customer-first").value = customer?.firstName ?? "";
  document.querySelector("#customer-last").value = customer?.lastName ?? "";
  document.querySelector("#customer-phone").value = customer?.phone ?? "";
  document.querySelector("#customer-birth").value = customer?.birthDate ?? "";
  document.querySelector("#customer-status").value = customer?.status ?? "ACTIVE";
  customerForm.classList.remove("hidden");
}

function closeCustomerForm() {
  state.editingCustomerId = null;
  customerForm.classList.add("hidden");
  customerForm.reset();
}

async function saveCustomer(event) {
  event.preventDefault();
  const body = {
    firstName: document.querySelector("#customer-first").value.trim(),
    lastName: document.querySelector("#customer-last").value.trim(),
    phone: document.querySelector("#customer-phone").value.trim(),
    birthDate: document.querySelector("#customer-birth").value || null,
    status: document.querySelector("#customer-status").value
  };
  const editing = state.editingCustomerId;
  try {
    const saved = await api(editing ? `/api/v1/customers/${editing}` : "/api/v1/customers", {
      method: editing ? "PATCH" : "POST",
      body: JSON.stringify(body)
    });
    if (!editing) {
      state.customerId = saved.id;
    }
    closeCustomerForm();
    showToast(editing ? "Клиент обновлён" : `Клиент №${saved.id} создан`);
    await loadAdmin();
  } catch (error) {
    showToast(error.message);
  }
}

async function toggleCustomer(customerId) {
  const customer = state.customers.find((item) => item.id === customerId);
  if (!customer) {
    return;
  }
  try {
    await api(`/api/v1/customers/${customerId}`, {
      method: "PATCH",
      body: JSON.stringify({
        firstName: customer.firstName,
        lastName: customer.lastName,
        phone: customer.phone,
        birthDate: customer.birthDate,
        status: customer.status === "ACTIVE" ? "BLOCKED" : "ACTIVE"
      })
    });
    showToast(customer.status === "ACTIVE" ? "Клиент заблокирован" : "Клиент разблокирован");
    await loadAdmin();
  } catch (error) {
    showToast(error.message);
  }
}

function renderOrderFilters() {
  const options = { ALL: "Все", ...ORDER_STATUS };
  orderFilters.innerHTML = Object.entries(options).map(([key, label]) => `
    <button class="filter ${key === state.orderFilter ? "is-active" : ""}" type="button" data-order-status="${key}">${label}</button>
  `).join("");
  orderFilters.querySelectorAll("[data-order-status]").forEach((button) => {
    button.addEventListener("click", () => {
      state.orderFilter = button.dataset.orderStatus;
      renderOrderFilters();
      renderOrderAdmin();
    });
  });
}

function renderOrderAdmin() {
  const visible = state.orders.filter((order) => state.orderFilter === "ALL" || order.status === state.orderFilter);
  if (visible.length === 0) {
    orderAdminList.innerHTML = `<div class="empty">Заказов с таким статусом нет.</div>`;
    return;
  }
  orderAdminList.innerHTML = visible.map((order) => {
    const customer = state.customers.find((item) => item.id === order.customerId);
    const name = customer ? `${customer.firstName} ${customer.lastName}` : `Клиент ${order.customerId}`;
    const items = order.items.map((item) => `${escapeHtml(item.productName)} × ${item.quantity}`).join(", ");
    return `
      <article class="admin-order">
        <div class="person-top">
          <div>
            <strong>Заказ №${order.id}</strong>
            <div class="meta">${escapeHtml(name)} · ${formatDate(order.createdAt)}</div>
          </div>
          <span class="badge ${order.status}">${ORDER_STATUS[order.status]}</span>
        </div>
        <div class="meta">${items}</div>
        <div class="price-row"><span class="meta">Итого</span><strong>${money(order.totalAmount)}</strong></div>
        <div class="row-actions">${orderActions(order)}</div>
      </article>
    `;
  }).join("");
  orderAdminList.querySelectorAll("[data-status]").forEach((button) => {
    button.addEventListener("click", () => changeOrderStatus(Number(button.dataset.status), button.dataset.target));
  });
  orderAdminList.querySelectorAll("[data-edit-order]").forEach((button) => {
    button.addEventListener("click", () => openOrderForm(Number(button.dataset.editOrder)));
  });
}

function editableProducts(order) {
  const used = new Set((order?.items ?? state.editLines).map((item) => item.productId));
  return state.products.filter((product) => product.status === "ACTIVE" || used.has(product.id));
}

function openOrderForm(orderId) {
  const order = state.orders.find((item) => item.id === orderId);
  if (!order) {
    return;
  }
  state.editingOrderId = orderId;
  state.editLines = order.items.map((item) => ({ productId: item.productId, quantity: item.quantity }));
  const select = document.querySelector("#order-customer");
  select.innerHTML = state.customers
    .filter((customer) => customer.status === "ACTIVE" || customer.id === order.customerId)
    .map((customer) => `<option value="${customer.id}">${escapeHtml(customerLabel(customer))}</option>`)
    .join("");
  select.value = String(order.customerId);
  const host = state.view === "admin" ? orderAdminList : ordersList;
  host.before(orderForm);
  orderForm.classList.remove("hidden");
  renderOrderLines();
  orderForm.scrollIntoView({ block: "nearest" });
}

function closeOrderForm() {
  state.editingOrderId = null;
  state.editLines = [];
  orderForm.classList.add("hidden");
}

function renderOrderLines() {
  const products = editableProducts();
  orderLines.innerHTML = state.editLines.map((line, index) => `
    <div class="order-line">
      <select aria-label="Товар" data-line-product="${index}">
        ${products.map((product) => `
          <option value="${product.id}" ${product.id === line.productId ? "selected" : ""}>${escapeHtml(product.name)}</option>
        `).join("")}
      </select>
      <input type="number" min="1" max="20" value="${line.quantity}" aria-label="Количество" data-line-qty="${index}">
      <button class="button button-ghost button-small" type="button" data-line-remove="${index}">Убрать</button>
    </div>
  `).join("");
  orderLines.querySelectorAll("[data-line-product]").forEach((select) => {
    select.addEventListener("change", () => {
      state.editLines[Number(select.dataset.lineProduct)].productId = Number(select.value);
    });
  });
  orderLines.querySelectorAll("[data-line-qty]").forEach((input) => {
    input.addEventListener("input", () => {
      state.editLines[Number(input.dataset.lineQty)].quantity = Number(input.value);
    });
  });
  orderLines.querySelectorAll("[data-line-remove]").forEach((button) => {
    button.addEventListener("click", () => {
      state.editLines.splice(Number(button.dataset.lineRemove), 1);
      renderOrderLines();
    });
  });
}

async function saveOrder(event) {
  event.preventDefault();
  const items = state.editLines
    .filter((line) => line.productId && line.quantity > 0)
    .map((line) => ({ productId: Number(line.productId), quantity: Number(line.quantity) }));
  try {
    await api(`/api/v1/orders/${state.editingOrderId}`, {
      method: "PATCH",
      body: JSON.stringify({
        customerId: Number(document.querySelector("#order-customer").value),
        items
      })
    });
    closeOrderForm();
    showToast("Заказ обновлён");
    state.products = await api("/api/v1/products");
    renderProducts();
    if (state.view === "orders") {
      await loadOrders();
    } else {
      await loadAdmin();
    }
  } catch (error) {
    showToast(error.message);
  }
}

function orderActions(order) {
  if (order.status === "NEW") {
    return `
      <button class="button button-ghost button-small" type="button" data-edit-order="${order.id}">Изменить</button>
      <button class="button button-solid button-small" type="button" data-status="${order.id}" data-target="PAID">Оплачен</button>
      <button class="button button-ghost button-small" type="button" data-status="${order.id}" data-target="CANCELLED">Отменить</button>
    `;
  }
  if (order.status === "PAID") {
    return `<button class="button button-solid button-small" type="button" data-status="${order.id}" data-target="COMPLETED">Завершить</button>`;
  }
  return "";
}

async function changeOrderStatus(orderId, status) {
  try {
    await api(`/api/v1/orders/${orderId}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status })
    });
    showToast(`Заказ №${orderId}: ${ORDER_STATUS[status]}`);
    state.products = await api("/api/v1/products");
    if (state.view === "catalog") {
      renderProducts();
    }
    await loadAdmin();
  } catch (error) {
    showToast(error.message);
  }
}

function openCart() {
  renderCart();
  document.querySelector("#cart-drawer").classList.remove("hidden");
  document.querySelector("#overlay").classList.remove("hidden");
}

function closeCart() {
  document.querySelector("#cart-drawer").classList.add("hidden");
  document.querySelector("#overlay").classList.add("hidden");
}

function ensureCart() {
  if (cartItems().length === 0) {
    showNotice("Сначала добавьте товары", "bad");
    return false;
  }
  return true;
}

function showNotice(message, kind) {
  cartNotice.textContent = message;
  cartNotice.className = `notice ${kind}`;
}

function clearNotice() {
  cartNotice.className = "notice hidden";
  cartNotice.textContent = "";
}

function selectedCustomer() {
  return state.customers.find((customer) => customer.id === state.customerId);
}

function showToast(message) {
  toast.textContent = message;
  toast.classList.remove("hidden");
  clearTimeout(showToast.timer);
  showToast.timer = setTimeout(() => toast.classList.add("hidden"), 2800);
}

async function api(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: {
      Accept: "application/json",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...options.headers
    }
  });
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    throw new Error(data?.message || "Не удалось выполнить запрос");
  }
  return data;
}

function money(value) {
  return new Intl.NumberFormat("ru-RU", {
    style: "currency",
    currency: "RUB",
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  }).format(Number(value));
}

function formatDate(value) {
  return new Intl.DateTimeFormat("ru-RU", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}

function formatBirthDate(value) {
  if (!value) {
    return "дата рождения не указана";
  }
  const [year, month, day] = value.split("-");
  return `${day}.${month}.${year}`;
}

function customerLabel(customer) {
  const blocked = customer.status === "BLOCKED" ? " · заблокирован" : "";
  const birth = customer.birthDate ? ` · ${formatBirthDate(customer.birthDate)}` : "";
  return `${customer.firstName} ${customer.lastName}${birth}${blocked}`;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

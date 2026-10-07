/* ============================================================
   STARLIGHT BOOKSTORE  Full Application Logic with Auth
   Connects to Spring Boot REST API on /api/*
   ============================================================ */

const API = 'http://localhost:8080';          
let books  = [];
let cart   = JSON.parse(localStorage.getItem('sb_cart') || '[]');

let currentUser = JSON.parse(localStorage.getItem('sb_user') || 'null');
let token = localStorage.getItem('sb_token') || null;

/* ============================================================
   BOOT
   ============================================================ */
document.addEventListener('DOMContentLoaded', () => {
  updateCartBadge();
  applyAuthState();
  const isAdminPage = window.location.pathname.includes('admin-');
  if (!isAdminPage && currentUser && currentUser.role === 'ADMIN') {
    window.location.href = 'admin-dashboard.html';
    return;
  }
  if (!isAdminPage) { fetchAndRenderHome(); }
});

/* ============================================================
   AUTH UI LOGIC
   ============================================================ */
function applyAuthState() {
  const isAuth = !!token;
  const isAdmin = currentUser && !currentUser.role.includes('CUSTOMER');

  document.querySelectorAll('.auth-only').forEach(el => el.style.display = isAuth ? 'block' : 'none');
  document.querySelectorAll('.guest-only').forEach(el => el.style.display = isAuth ? 'none' : 'block');
  document.querySelectorAll('.admin-only').forEach(el => el.style.display = isAdmin ? 'block' : 'none');

  // Specific inline element fixes
  document.querySelectorAll('span.auth-only').forEach(el => el.style.display = isAuth ? 'inline' : 'none');
  document.querySelectorAll('button.auth-only').forEach(el => el.style.display = isAuth ? 'inline-block' : 'none');
  document.querySelectorAll('button.guest-only').forEach(el => el.style.display = isAuth ? 'none' : 'inline-block');
  document.querySelectorAll('li.auth-only').forEach(el => el.style.display = isAuth ? 'inline-block' : 'none');
  document.querySelectorAll('li.admin-only').forEach(el => el.style.display = isAdmin ? 'inline-block' : 'none');

  // Hide cart and shopping features from admins
  document.querySelectorAll('.no-admin').forEach(el => {
    if (isAdmin) {
      el.style.setProperty('display', 'none', 'important');
    } else {
      el.style.display = ''; // Revert to CSS default
    }
  });

  if (isAuth && currentUser) {
    document.getElementById('userNameDisplay') && (document.getElementById('userNameDisplay').textContent = `Hello, ${currentUser.username}`);
    updateNotificationCount();
    const navUserEl = document.getElementById('topNavUserName');
    if (navUserEl) navUserEl.textContent = currentUser.username;
    
    // Update profile pictures in UI
    if (currentUser.profilePicture && currentUser.profilePicture !== "") {
        const picUrl = '/uploads/profiles/' + currentUser.profilePicture;
        
        const topNavProfileImg = document.getElementById('topNavProfileImg');
        const topNavProfileIcon = document.getElementById('topNavProfileIcon');
        if (topNavProfileImg && topNavProfileIcon) {
            topNavProfileImg.src = picUrl;
            topNavProfileImg.style.display = 'block';
            topNavProfileIcon.style.display = 'none';
        }
        
        const wwCombinedProfileImg = document.getElementById('wwCombinedProfileImg');
        if (wwCombinedProfileImg) {
            wwCombinedProfileImg.src = picUrl;
        }
    }
  }
}

function showAuthPage(mode) {
  document.getElementById('authMode').value = mode;
  showSection('auth');
  
  if (mode === 'register') {
    document.getElementById('authModalIcon').textContent = '';
    document.getElementById('authModalTitle').textContent = 'Create Account';
    document.getElementById('authEmailGroup').style.display = 'block';
    document.getElementById('authEmail').required = true;
    document.getElementById('authSubmitBtn').textContent = 'Sign Up ';
    document.getElementById('authToggleLink').textContent = 'Already have an account? Sign In ';
  } else {
    document.getElementById('authModalIcon').textContent = '';
    document.getElementById('authModalTitle').textContent = 'Welcome Back';
    document.getElementById('authEmailGroup').style.display = 'none';
    document.getElementById('authEmail').required = false;
    document.getElementById('authSubmitBtn').textContent = 'Sign In';
    document.getElementById('authToggleLink').textContent = 'Need an account? Sign up ';
  }
}

function toggleAuthMode() {
  const current = document.getElementById('authMode').value;
  showAuthPage(current === 'login' ? 'register' : 'login');
}

async function submitAuth(e) {
  e.preventDefault();
  const mode = document.getElementById('authMode').value;
  const username = document.getElementById('authUsername').value;
  const password = document.getElementById('authPassword').value;

  if (mode === 'login') {
    try {
      const res = await fetch(API + '/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      if (!res.ok) {
        const err = await res.json().catch(()=>({}));
        throw new Error(err.message || 'Invalid credentials');
      }
      const data = await res.json();
      token = data.token;
      currentUser = data;
      localStorage.setItem('sb_token', token);
      localStorage.setItem('sb_user', JSON.stringify(currentUser));
      applyAuthState();
      showToast('Logged in successfully!', 'success');
      
      if (currentUser.role === 'ADMIN') {
        showSection('admin');
      } else {
        showSection('home');
      }
    } catch (err) {
      showToast(err.message, 'error');
    }
  } else {
    const email = document.getElementById('authEmail').value;
    try {
      const res = await fetch(API + '/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, email, password })
      });
      if (!res.ok) {
        const err = await res.json().catch(()=>({}));
        throw new Error(err.message || 'Registration failed');
      }
      showToast('Registration successful! Please sign in.', 'success');
      showAuthPage('login');
    } catch (err) {
      showToast(err.message, 'error');
    }
  }
}

function logout() {
  token = null;
  currentUser = null;
  localStorage.removeItem('sb_token');
  localStorage.removeItem('sb_user');
  applyAuthState();
  showSection('home');
  showToast('Logged out.', 'success');
}

function showCenterMessage(msg) {
  let modal = document.getElementById('centerMessageModal');
  if (!modal) {
    modal = document.createElement('div');
    modal.id = 'centerMessageModal';
    modal.style.position = 'fixed';
    modal.style.top = '0';
    modal.style.left = '0';
    modal.style.width = '100vw';
    modal.style.height = '100vh';
    modal.style.background = 'rgba(0,0,0,0.8)';
    modal.style.display = 'flex';
    modal.style.justifyContent = 'center';
    modal.style.alignItems = 'center';
    modal.style.zIndex = '10000';
    
    const content = document.createElement('div');
    content.className = 'glass-panel scale-in';
    content.style.padding = '3rem';
    content.style.textAlign = 'center';
    content.style.maxWidth = '400px';
    content.style.border = '2px solid var(--pink)';
    
    const icon = document.createElement('div');
    icon.innerHTML = '⚠️';
    icon.style.fontSize = '4rem';
    icon.style.marginBottom = '1rem';
    
    const text = document.createElement('h3');
    text.id = 'centerMessageText';
    text.style.color = 'var(--text)';
    text.style.marginBottom = '2rem';
    
    const btn = document.createElement('button');
    btn.className = 'btn-primary';
    btn.textContent = 'Okay';
    btn.onclick = () => modal.style.display = 'none';
    
    content.appendChild(icon);
    content.appendChild(text);
    content.appendChild(btn);
    modal.appendChild(content);
    document.body.appendChild(modal);
  }
  
  document.getElementById('centerMessageText').textContent = msg;
  modal.style.display = 'flex';
}

/* ============================================================
   API HELPERS
   ============================================================ */
async function apiFetch(url, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  if (token) {
    headers['Authorization'] = 'Bearer ' + token;
  }
  
  // Prevent GET caching
  const fetchOptions = { ...options, headers };
  if (!fetchOptions.method || fetchOptions.method.toUpperCase() === 'GET') {
    fetchOptions.cache = 'no-store';
  }
  
  const res = await fetch(API + url, fetchOptions);
  
  if (res.status === 401) {
    logoutMPA();
    throw new Error('Unauthorized');
  }
  
  if (!res.ok) {
     const err = await res.text();
     let errorMsg = `HTTP ${res.status}`;
     try { 
         const j = JSON.parse(err); 
         if (j.message) errorMsg = j.message; 
     } catch (e) {}
     throw new Error(errorMsg);
  }
  if (res.status === 204) return null;
  return res.json();
}

function enforceRole(requiredRole) {
  if (!token || !currentUser) {
    window.location.href = 'login.html';
    return;
  }
  if (requiredRole && currentUser.role !== requiredRole && currentUser.role !== 'ADMIN') {
    window.location.href = 'login.html';
  }
}

function logoutMPA() {
  localStorage.removeItem('sb_token');
  localStorage.removeItem('sb_user');
  window.location.href = 'login.html';
}

/* ============================================================
   PAGE TRANSITION (3D BOOK)
   ============================================================ */
function injectTransitionOverlay() {
  if (document.getElementById('page-transition-overlay')) return;
  const overlay = document.createElement('div');
  overlay.id = 'page-transition-overlay';
  overlay.innerHTML = `
    <div class="transition-book">
      <div class="transition-page"></div>
      <div class="transition-page"></div>
      <div class="transition-page"></div>
      <div class="transition-cover"> Starlight</div>
    </div>
  `;
  document.body.appendChild(overlay);
}

function playPageTransition(callbackOrUrl, isSpa = false) {
  injectTransitionOverlay();
  const overlay = document.getElementById('page-transition-overlay');
  overlay.classList.add('active');
  
  // Force reflow
  void overlay.offsetWidth;
  
  overlay.classList.add('animating');
  
  setTimeout(() => {
    if (typeof callbackOrUrl === 'function') {
      callbackOrUrl();
    } else if (isSpa) {
      showSection(callbackOrUrl);
    } else {
      window.location.href = callbackOrUrl;
    }
  }, 900); // Swap content halfway through the 1.5s animation
  
  setTimeout(() => {
    overlay.classList.remove('active', 'animating');
  }, 1800);
}

// Intercept anchors globally and bind advanced catalog filters
document.addEventListener('DOMContentLoaded', () => {
  injectTransitionOverlay();
  document.addEventListener('click', e => {
    const a = e.target.closest('a');
    if (!a) return;
    const href = a.getAttribute('href');
    if (href && href.endsWith('.html') && a.target !== '_blank' && !a.hasAttribute('onclick')) {
      e.preventDefault();
      playPageTransition(href, false);
    }
  });

  // Bind catalog filters to automatically trigger search on change
  const filterInputs = document.querySelectorAll('.filter-cat, #filterLang, #filterMinPrice, #filterMaxPrice, #filterRating, input[name="availability"]');
  filterInputs.forEach(input => {
    input.addEventListener('change', applyAdvancedFilters);
  });
  
  // The price inputs should also trigger on 'input' but with debounce, which is handled in applyAdvancedFilters
  const priceInputs = document.querySelectorAll('#filterMinPrice, #filterMaxPrice');
  priceInputs.forEach(input => {
    input.addEventListener('input', applyAdvancedFilters);
  });
});

/* ============================================================
   NAVIGATION
   ============================================================ */
function navClick(e, section) {
  e && e.preventDefault();
  
  // Guard auth routes
  if (['orders', 'support', 'profile', 'wishlist'].includes(section) && (!token)) {
    showToast('Please log in first.', 'error');
    playPageTransition('login.html', false);
    return;
  }
  if (section === 'admin' && (!currentUser || currentUser.role === 'CUSTOMER')) {
    return; // block
  }

  playPageTransition(section, true);
}

function showSection(id) {
  document.querySelectorAll('.section').forEach(s => {
    s.classList.remove('active-section');
    s.classList.add('hidden-section');
    s.style.display = 'none';
  });
  const target = document.getElementById(id);
  if (target) {
    target.style.display = 'block';
    target.classList.add('active-section');
    target.classList.remove('hidden-section');
  }

  // Update nav active state
  document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
  const navEl = document.getElementById('nav-' + id);
  if (navEl) navEl.classList.add('active');

  // Load data for each section
  if (id === 'home')    fetchAndRenderHome();
  if (id === 'catalog') loadCatalog();
  if (id === 'cart')    renderCart();
  if (id === 'orders')  loadOrders();
  if (id === 'support') loadSupport();
  if (id === 'admin')   loadAdmin();
  if (id === 'profile') loadProfile();
  if (id === 'wishlist') loadWishlist();
}

/* ============================================================
   HOME
   ============================================================ */
async function fetchAndRenderHome() {
  try {
    const stats = await apiFetch('/api/dashboard/stats');
    document.getElementById('statBooks').textContent   = stats.totalBooks;
    document.getElementById('statOrders').textContent  = stats.totalOrders;
    document.getElementById('statRevenue').textContent = 'Rs ' + Number(stats.totalRevenue).toFixed(0);
  } catch {}

  try {
    books = await apiFetch('/api/books');
    renderBookGrid('featuredGrid', books.slice(0, 8));
    
    // Render Popular Books (From Orders)
    const popularContainer = document.getElementById('popularBooksContainer');
    if (popularContainer) {
      let orderedBooksMap = new Map();
      try {
        const orders = await apiFetch('/api/orders');
        orders.forEach(o => {
          if (o.items) {
            o.items.forEach(i => {
              if (i.book) {
                orderedBooksMap.set(i.book.id, i.book);
              }
            });
          }
        });
      } catch (err) {}
      
      let popular = Array.from(orderedBooksMap.values());
      if (popular.length === 0) {
        popular = [...books].sort(() => 0.5 - Math.random()).slice(0, 6);
      } else {
        popular = popular.slice(0, 8);
      }
      
      popularContainer.innerHTML = popular.map(b => `
        <div style="flex: 0 0 140px; cursor: pointer; transition: transform 0.3s;" onmouseover="this.style.transform='translateY(-5px)'" onmouseout="this.style.transform='translateY(0)'" onclick="viewBookDetail(${b.id})">
          <div style="height: 200px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.5); margin-bottom: 0.8rem; border: 1px solid rgba(212,175,55,0.2);">
            ${b.coverImage ? `<img src="/uploads/covers/${b.coverImage}" style="width:100%; height:100%; object-fit:cover;"/>` : `<div style="width:100%; height:100%; background: #333;"></div>`}
          </div>
          <h5 style="margin: 0; font-family: 'Inter', sans-serif; font-size: 0.95rem; color: #fff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${b.title}</h5>
          <div style="font-size: 0.8rem; color: rgba(255,255,255,0.5); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 0.2rem;">${b.author}</div>
          <div style="color: #ffd700; font-size: 0.75rem;"><i class="fa-solid fa-star"></i> <i class="fa-solid fa-star"></i> <i class="fa-solid fa-star"></i> <i class="fa-solid fa-star"></i> <i class="fa-solid fa-star-half-stroke"></i> <span style="color: #fff; margin-left: 0.2rem;">4.8</span></div>
        </div>
      `).join('');
    }
    
    // Render New Arrivals
    const newArrivalsContainer = document.getElementById('newArrivalsContainer');
    if (newArrivalsContainer) {
      // Newest books (highest ID)
      const newest = [...books].sort((a,b) => b.id - a.id).slice(0, 3);
      newArrivalsContainer.innerHTML = newest.map(b => `
        <div style="display: flex; gap: 1rem; cursor: pointer; background: rgba(0,0,0,0.3); padding: 0.5rem; border-radius: 8px; border: 1px solid rgba(212,175,55,0.1); transition: background 0.3s;" onmouseover="this.style.background='rgba(212,175,55,0.1)'" onmouseout="this.style.background='rgba(0,0,0,0.3)'" onclick="viewBookDetail(${b.id})">
          <div style="width: 60px; height: 85px; border-radius: 4px; overflow: hidden; flex-shrink: 0;">
            ${b.coverImage ? `<img src="/uploads/covers/${b.coverImage}" style="width:100%; height:100%; object-fit:cover;"/>` : `<div style="width:100%; height:100%; background: #333;"></div>`}
          </div>
          <div style="display: flex; flex-direction: column; justify-content: center; overflow: hidden;">
            <span style="background: rgba(212,175,55,0.2); color: var(--primary); font-size: 0.65rem; padding: 0.1rem 0.4rem; border-radius: 10px; width: fit-content; margin-bottom: 0.3rem;">NEW</span>
            <h5 style="margin: 0; font-family: 'Inter', sans-serif; font-size: 0.95rem; color: #fff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${b.title}</h5>
            <div style="font-size: 0.8rem; color: rgba(255,255,255,0.5); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${b.author}</div>
            <div style="color: #ffd700; font-size: 0.75rem; margin-top: 0.2rem;"><i class="fa-solid fa-star"></i> <i class="fa-solid fa-star"></i> <i class="fa-solid fa-star"></i> <i class="fa-solid fa-star"></i> <i class="fa-solid fa-star"></i></div>
          </div>
        </div>
      `).join('');
    }
  } catch {}
  
  if (typeof initWelcomeWidget === 'function') {
    initWelcomeWidget();
  }
}

/* ============================================================
   CATALOG
   ============================================================ */
async function loadCatalog() {
  try { books = await apiFetch('/api/books'); } catch {}
  renderBookGrid('catalogGrid', books);
}

function handleSearch() {
  const q = document.getElementById('searchInput').value.toLowerCase().trim();
  const filtered = q
    ? books.filter(b => b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q))
    : books;
  renderBookGrid('catalogGrid', filtered);
}

function filterByGenre(el, genre) {
  document.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
  el.classList.add('active');
  const filtered = genre ? books.filter(b => (b.genre || '').toLowerCase().includes(genre.toLowerCase())) : books;
  renderBookGrid('featuredGrid', filtered.slice(0, 8));
}

function renderBookGrid(containerId, bookList) {
  const grid = document.getElementById(containerId);
  if (!grid) return;
  if (!bookList || bookList.length === 0) {
    grid.innerHTML = '<p class="text-muted" style="padding:2rem;">No books found.</p>';
    return;
  }
  grid.innerHTML = bookList.map((b, i) => `
    <div class="book-card" style="animation-delay:${i * 0.06}s">
      <div class="book-cover-art" style="background:${coverGradient(i)}">${b.coverImage ? `<img src="/uploads/covers/${b.coverImage}" style="width:100%; height:100%; object-fit:cover; border-radius:inherit;"/>` : ''}</div>
      <h3>${b.title}</h3>
      <p class="author">by ${b.author}</p>
      ${b.genre ? `<span class="genre-tag">${b.genre}</span>` : ''}
      <div class="price">Rs ${Number(b.price).toFixed(2)}</div>
      <div class="stock ${b.stock <= 5 ? 'stock-low' : ''}">
        ${b.stock > 0 ? `${b.stock} in stock` : ' Out of stock'}
      </div>
      <div style="display:flex;gap:0.5rem;">
        <button class="btn-primary btn-sm w-100 no-admin" onclick="addToCart(${b.id}, event)" ${b.stock === 0 ? 'disabled' : ''}>Add to Cart </button>
        ${token ? `<button class="btn-secondary btn-sm" onclick="addToWishlist(${b.id}, event)" title="Add to Wishlist"></button>` : ''}
      </div>
    </div>`).join('');
}

function coverGradient(i) {
  const gradients = ['linear-gradient(135deg,#3b82f6,#8b5cf6)','linear-gradient(135deg,#8b5cf6,#ec4899)','linear-gradient(135deg,#ec4899,#f97316)'];
  return gradients[i % gradients.length];
}

/* ============================================================
   CART & CHECKOUT (API BACKED)
   ============================================================ */

let cartData = null; // Stores cart from backend

async function updateCartBadge() {
  if (!token || (currentUser && !currentUser.role.includes('CUSTOMER'))) return;
  try {
    const data = await apiFetch('/cart/count');
    const badge = document.getElementById('cartBadge');
    if (badge) {
      badge.textContent = data.count || 0;
    }
  } catch(e) { }
}

async function addToCart(bookId, event, customQty = 1) {
  if (!token) {
    showToast('Please sign in to add to cart.', 'error');
    setTimeout(() => window.location.href = 'login.html', 1500);
    return;
  }
  
  if (event) {
    const btn = event.currentTarget;
    const card = btn.closest('.book-card') || btn.closest('.glass-panel');
    if (card) {
      const cover = card.querySelector('.book-cover-art') || document.getElementById('detailCover');
      const cartIcon = document.getElementById('cartBadge')?.parentElement || document.querySelector('a[href="cart.html"]');
      
      if (cover && cartIcon) {
        const coverRect = cover.getBoundingClientRect();
        const cartRect = cartIcon.getBoundingClientRect();
        
        const clone = cover.cloneNode(true);
        clone.className = 'flying-book';
        clone.style.width = coverRect.width + 'px';
        clone.style.height = coverRect.height + 'px';
        clone.style.top = coverRect.top + 'px';
        clone.style.left = coverRect.left + 'px';
        
        document.body.appendChild(clone);
        
        void clone.offsetWidth;
        
        const flyX = cartRect.left - coverRect.left + (cartRect.width/2) - (coverRect.width/2);
        const flyY = cartRect.top - coverRect.top + (cartRect.height/2) - (coverRect.height/2);
        
        clone.style.transform = `translate(${flyX}px, ${flyY}px) scale(0.2)`;
        clone.style.opacity = '0';
        
        setTimeout(() => {
          if (clone.parentNode) clone.parentNode.removeChild(clone);
          finishAddToCart(bookId, customQty);
        }, 800);
        return;
      }
    }
  }
  
  finishAddToCart(bookId, customQty);
}

async function finishAddToCart(bookId, quantity) {
  try {
    await apiFetch(`/cart/add/${bookId}`, {
      method: 'POST',
      body: JSON.stringify({ quantity })
    });
    showToast('Added to cart! ', 'success');
    updateCartBadge();
    setTimeout(() => {
      window.location.href = 'cart.html';
    }, 500);
  } catch (err) {
    console.error('Cart API Error:', err);
    if (err.message && err.message.toLowerCase().includes('stock')) {
      showCenterMessage("No more stock available!");
    } else {
      showToast(err.message || 'Failed to add to cart', 'error');
    }
  }
}

async function loadCartPage() {
  if (!token) {
    window.location.href = 'login.html';
    return;
  }
  
  const container = document.getElementById('cartItemsList');
  const sidebar = document.getElementById('cartSidebar');
  const emptyEl = document.getElementById('cartEmpty');
  
  if (!container) return; // not on cart page
  
  try {
    cartData = await apiFetch('/cart');
    
    if (!cartData.items || cartData.items.length === 0) {
      if (emptyEl) emptyEl.style.display = 'block';
      if (sidebar) sidebar.style.display = 'none';
      container.style.display = 'none';
      container.innerHTML = '';
      document.getElementById('cartItemCount').textContent = '0 Items';
      return;
    }
    
    if (emptyEl) emptyEl.style.display = 'none';
    if (sidebar) sidebar.style.display = 'block';
    container.style.display = 'flex';
    
    let totalItems = 0;
    let subtotal = 0;
    
    container.innerHTML = cartData.items.map(item => {
      totalItems += item.quantity;
      const price = item.book.price;
      const st = price * item.quantity;
      subtotal += st;
      
      return `
      <div class="cart-item glass-panel" style="display:flex; gap:1.5rem; align-items:center; padding:1.5rem; animation: fade-in-up 0.3s ease;">
        <div class="cart-item-emoji" style="font-size:3rem; background:var(--surface); padding:1rem; border-radius:12px;">${item.book.coverImage ? `<img src="/uploads/covers/${item.book.coverImage}" style="width:100%; height:100%; object-fit:cover; border-radius:inherit;"/>` : ''}</div>
        <div style="flex:1;">
          <h4 style="font-size:1.2rem; margin-bottom:0.3rem;">${item.book.title}</h4>
          <p class="text-muted" style="margin-bottom:0.5rem;">by ${item.book.author}</p>
          <div style="font-weight:700; color:var(--primary); font-size:1.2rem;">Rs ${Number(price).toFixed(2)}</div>
        </div>
        <div style="display:flex; flex-direction:column; align-items:flex-end; gap:1rem;">
          <div style="display:flex; align-items:center; gap:0.5rem; background:var(--surface); padding:0.3rem; border-radius:8px;">
            <button class="btn-ghost btn-sm" style="padding:0.2rem 0.6rem; border:1px solid var(--glass-b);" onclick="changeQty(${item.id}, ${item.quantity - 1})"></button>
            <span style="font-weight:600; width:20px; text-align:center;">${item.quantity}</span>
            <button class="btn-ghost btn-sm" style="padding:0.2rem 0.6rem; border:1px solid var(--glass-b);" onclick="changeQty(${item.id}, ${item.quantity + 1})">+</button>
          </div>
          <div style="font-size:1.1rem; font-weight:600;">Rs ${Number(st).toFixed(2)}</div>
          <div style="display:flex; gap:0.5rem;">
            <button class="btn-ghost btn-sm text-pink" style="font-size:0.85rem;" onclick="removeFromCart(${item.id})">Remove</button>
            <button class="btn-ghost btn-sm" style="font-size:0.85rem;" onclick="moveToWishlist(${item.id})">Move to Wishlist</button>
          </div>
        </div>
      </div>`;
    }).join('');
    
    document.getElementById('cartItemCount').textContent = `${totalItems} Items`;
    
    // Calculate Summary
    let discount = 0;
    if (subtotal > 3000) discount = subtotal * 0.05;
    const tax = 0;
    const total = subtotal - discount + tax;

    document.getElementById('summarySubtotal').textContent = 'Rs ' + subtotal.toFixed(2);
    document.getElementById('summaryTax').textContent = 'Rs ' + tax.toFixed(2);
    const discEl = document.getElementById('summaryDiscount');
    if (discEl) discEl.textContent = '-Rs ' + discount.toFixed(2);
    document.getElementById('summaryTotal').textContent = 'Rs ' + total.toFixed(2);
    
    const clearBtn = document.querySelector('button[onclick="clearCart()"]');
    if (clearBtn) clearBtn.style.display = 'block';
    
  } catch (err) {
    console.error('Cart API Error:', err);
    showToast('Failed to load cart.', 'error');
  }
}

async function changeQty(cartItemId, newQty) {
  if (newQty < 1) {
    removeFromCart(cartItemId);
    return;
  }
  try {
    await apiFetch(`/cart/update/${cartItemId}`, {
      method: 'PUT',
      body: JSON.stringify({ quantity: newQty })
    });
    loadCartPage();
    updateCartBadge();
  } catch (err) {
    if (err.message && err.message.toLowerCase().includes('stock')) {
      showCenterMessage("No more stock available!");
    } else {
      showToast(err.message, 'error');
    }
  }
}

async function removeFromCart(cartItemId) {
  try {
    await apiFetch(`/cart/remove/${cartItemId}`, { method: 'DELETE' });
    showToast('Item removed', 'success');
    loadCartPage();
    updateCartBadge();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function clearCart() {
  try {
    await apiFetch('/cart/clear', { method: 'DELETE' });
    showToast('Cart cleared', 'success');
    loadCartPage();
    updateCartBadge();
    const clearBtn = document.querySelector('button[onclick="clearCart()"]');
    if (clearBtn) clearBtn.style.display = 'none';
  } catch (err) {
    console.error('Cart API Error:', err);
    showToast('Failed to clear cart', 'error');
  }
}

async function moveToWishlist(cartItemId) {
  try {
    await apiFetch(`/cart/move-to-wishlist/${cartItemId}`, { method: 'POST' });
    showToast('Item moved to wishlist', 'success');
    loadCartPage();
    updateCartBadge();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function addToWishlist(bookId, event) {
  if (!token) {
    showToast('Please sign in to add to wishlist.', 'error');
    setTimeout(() => window.location.href = 'login.html', 1500);
    return;
  }
  if (event) {
    const btn = event.currentTarget;
    const card = btn.closest('.book-card');
    if (card) {
      const bookmark = document.createElement('div');
      bookmark.className = 'wishlist-bookmark';
      card.appendChild(bookmark);
      card.style.boxShadow = '0 0 20px var(--gold-glow)';
      card.style.transition = 'box-shadow 0.3s';
      setTimeout(() => { card.style.boxShadow = ''; }, 1000);
    }
  }
  try {
    await apiFetch(`/wishlist/add/${bookId}`, { method: 'POST' });
    showToast('Added to Wishlist ', 'success');
  } catch(err) {
    showToast(err.message || 'Already in Wishlist or Error', 'error');
  }
}


/* ============================================================
   USER DASHBOARD (ORDERS & WISHLIST)
   ============================================================ */
async function loadOrders() {
  try {
    // If Admin, show all. If User, the backend needs an endpoint to filter, or we filter on frontend for demo
    // Wait, the backend doesn't have a specific /my-orders endpoint. Let's filter on frontend for demo.
    const allOrders = await apiFetch('/api/orders');
    const myOrders = currentUser.role === 'CUSTOMER' 
      ? allOrders.filter(o => o.user && o.user.id === currentUser.id)
      : allOrders;

    const tbody = document.getElementById('ordersBody');
    if (!myOrders.length) {
      tbody.innerHTML = '<tr><td colspan="7">No orders yet.</td></tr>';
      return;
    }
    tbody.innerHTML = myOrders.map(o => `
      <tr>
        <td>#${o.id}</td>
        <td>${o.customerName}</td>
        <td>${o.customerEmail}</td>
        <td>Rs ${Number(o.totalAmount).toFixed(2)}</td>
        <td><span class="badge badge-${(o.status||'').toLowerCase()}">${o.status}</span></td>
        <td>${o.createdAt ? o.createdAt.slice(0,3).join('-') : ''}</td>
      </tr>`).join('');
  } catch {}
}

async function loadWishlistPage() {
  const container = document.getElementById('wishlistItemsContainer');
  const emptyEl = document.getElementById('wishlistEmpty');
  if (!container) return; // not on wishlist page
  
  try {
    const wishlist = await apiFetch('/wishlist');
    
    if (!wishlist.items || wishlist.items.length === 0) {
      if (emptyEl) emptyEl.style.display = 'block';
      container.style.display = 'none';
      container.innerHTML = '';
      document.getElementById('wishlistItemCount').textContent = '0 Books';
      return;
    }
    
    if (emptyEl) emptyEl.style.display = 'none';
    container.style.display = 'grid';
    document.getElementById('wishlistItemCount').textContent = `${wishlist.items.length} Books`;
    
    container.innerHTML = wishlist.items.map(item => {
      const book = item.book;
      return `
      <div class="book-card" style="display:flex; flex-direction:column; justify-content:space-between; height:100%;" id="wishlistItem-${item.id}">
        <div>
          <div class="book-cover-art" style="font-size:4rem;">${book.coverImage ? `<img src="/uploads/covers/${book.coverImage}" style="width:100%; height:100%; object-fit:cover; border-radius:inherit;"/>` : ''}</div>
          <h3 style="margin-top:1rem;">${book.title}</h3>
          <p class="text-muted" style="margin-bottom:0.5rem;">${book.author}</p>
          <div class="rating text-gold"> ${book.rating}</div>
          <div class="price" style="font-size:1.5rem; color:var(--primary); font-weight:700; margin:1rem 0;">Rs ${book.price.toFixed(2)}</div>
        </div>
        <div style="display:flex; flex-direction:column; gap:0.5rem; margin-top:1rem;">
          <button class="btn-primary w-100" onclick="moveToCartFromWishlist(${item.id}, ${book.id}, event)">Move to Cart</button>
          <button class="btn-ghost text-pink w-100" style="border:1px solid var(--glass-b);" onclick="removeFromWishlist(${item.id})">Remove</button>
        </div>
      </div>
      `;
    }).join('');
  } catch(e) {
    console.error(e);
  }
}

async function removeFromWishlist(wishlistItemId) {
  try {
    const card = document.getElementById(`wishlistItem-${wishlistItemId}`);
    if (card) {
      card.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
      card.style.opacity = '0';
      card.style.transform = 'scale(0.9)';
    }
    
    await apiFetch(`/wishlist/remove/${wishlistItemId}`, { method: 'DELETE' });
    showToast('Removed from wishlist', 'success');
    
    setTimeout(() => {
      loadWishlistPage();
    }, 300);
  } catch(err) {
    showToast(err.message, 'error');
  }
}

async function moveToCartFromWishlist(wishlistItemId, bookId, event) {
  try {
    // Animation logic
    const btn = event.currentTarget;
    const card = btn.closest('.book-card');
    const coverArt = card.querySelector('.book-cover-art');
    const target = document.getElementById('cartIconTarget');
    
    if (coverArt && target) {
      const coverRect = coverArt.getBoundingClientRect();
      const targetRect = target.getBoundingClientRect();
      
      const clone = coverArt.cloneNode(true);
      clone.style.position = 'fixed';
      clone.style.left = coverRect.left + 'px';
      clone.style.top = coverRect.top + 'px';
      clone.style.width = coverRect.width + 'px';
      clone.style.height = coverRect.height + 'px';
      clone.style.zIndex = '9999';
      clone.style.transition = 'all 0.6s cubic-bezier(0.2, 1, 0.3, 1)';
      clone.style.margin = '0';
      clone.style.borderRadius = '8px';
      clone.style.opacity = '0.9';
      
      document.body.appendChild(clone);
      
      setTimeout(() => {
        clone.style.left = targetRect.left + 'px';
        clone.style.top = targetRect.top + 'px';
        clone.style.transform = 'scale(0.1)';
        clone.style.opacity = '0';
      }, 50);
      
      setTimeout(() => {
        clone.remove();
      }, 650);
    }
    
    if (card) {
      card.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
      card.style.opacity = '0';
      card.style.transform = 'scale(0.9)';
    }

    await apiFetch(`/wishlist/move-to-cart/${wishlistItemId}`, { method: 'POST' });
    updateCartBadge();
    showToast('Moved to cart! ', 'success');
    
    setTimeout(() => {
      loadWishlistPage();
    }, 300);
  } catch(err) {
    showToast(err.message, 'error');
  }
}

/* ============================================================
   PROFILE
   ============================================================ */
async function loadProfile() {
  try {
    const p = await apiFetch('/api/user/profile');
    document.getElementById('profUsername').value = p.username;
    document.getElementById('profEmail').value = p.email;
    document.getElementById('profRole').value = p.role;
    document.getElementById('profAddress').value = p.address || '';
  } catch {}
}

async function updateProfile(e) {
  e.preventDefault();
  const address = document.getElementById('profAddress').value;
  try {
    await apiFetch('/api/user/profile', { method:'PUT', body: JSON.stringify({address}) });
    showToast('Profile updated!', 'success');
  } catch {}
}

/* ============================================================
   SUPPORT
   ============================================================ */
async function loadSupport() {
  try {
    const all = await apiFetch('/api/support');
    const myTickets = currentUser.role === 'CUSTOMER' 
      ? all.filter(t => t.user && t.user.id === currentUser.id)
      : all;

    const list = document.getElementById('ticketsList');
    if (!myTickets.length) {
      list.innerHTML = '<p class="text-muted">No tickets submitted.</p>';
      return;
    }
    list.innerHTML = myTickets.map(t => `
      <div style="padding:0.8rem 0;border-bottom:1px solid var(--glass-b)">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <strong>${t.subject}</strong>
          <span class="badge badge-${(t.status||'open').toLowerCase()}">${t.status}</span>
        </div>
      </div>`).join('');
  } catch {}
}

async function submitTicket(e) {
  e.preventDefault();
  const ticket = {
    customerName:  document.getElementById('ticketName').value,
    customerEmail: document.getElementById('ticketEmail').value,
    subject:       document.getElementById('ticketSubject').value,
    message:       document.getElementById('ticketMessage').value
  };
  try {
    await apiFetch('/api/support', { method: 'POST', body: JSON.stringify(ticket) });
    document.getElementById('supportForm').reset();
    showToast('Ticket submitted! ', 'success');
    loadSupport();
  } catch {}
}

/* ============================================================
   ADMIN
   ============================================================ */
async function loadAdmin() {
  try {
    const stats = await apiFetch('/api/dashboard/stats');
    document.getElementById('scBooks').textContent   = stats.totalBooks;
    document.getElementById('scOrders').textContent  = stats.totalOrders;
    document.getElementById('scRevenue').textContent = 'Rs ' + Number(stats.totalRevenue).toFixed(2);
    document.getElementById('scTickets').textContent = stats.openTickets;
  } catch {}

  // Books table
  const tbodyBooks = document.getElementById('adminBooksBody');
  if (tbodyBooks) {
    if(books.length === 0) try { books = await apiFetch('/api/books'); } catch{}
    tbodyBooks.innerHTML = books.map(b => `
      <tr>
        <td>${b.coverImage ? `<img src="/uploads/covers/${b.coverImage}" style="width:100%; height:100%; object-fit:cover; border-radius:inherit;"/>` : ''} ${b.title}</td>
        <td>Rs ${Number(b.price).toFixed(2)}</td>
        <td class="${b.stock <= 5 ? 'stock-low' : ''}">${b.stock}</td>
        <td><button class="btn-red btn-sm" onclick="adminDeleteBook(${b.id})">Del</button></td>
      </tr>`).join('');
  }

  // Orders table
  const tbodyOrders = document.getElementById('adminOrdersBody');
  if (tbodyOrders) {
    try {
      const orders = await apiFetch('/api/orders');
      tbodyOrders.innerHTML = orders.map(o => `
        <tr>
          <td>#${o.id}</td><td>${o.customerName}</td><td>Rs ${Number(o.totalAmount).toFixed(2)}</td>
          <td>
            <select onchange="updateOrderStatus(${o.id}, this.value)" style="padding:0.3rem;border-radius:6px;">
              ${['PENDING','PACKED','SHIPPED','DELIVERED','CANCELLED'].map(s =>
                `<option value="${s}" ${o.status===s?'selected':''}>${s}</option>`).join('')}
            </select>
          </td>
        </tr>`).join('');
    } catch {}
  }

  // Tickets table
  const tbodyTickets = document.getElementById('adminTicketsBody');
  if (tbodyTickets) {
    try {
      const tickets = await apiFetch('/api/support');
      tbodyTickets.innerHTML = tickets.map(t => `
        <tr>
          <td>#${t.id}</td><td>${t.customerName}</td><td>${t.subject}</td><td>${t.status}</td>
          <td>
            <select onchange="updateTicketStatus(${t.id}, this.value)" style="padding:0.3rem;border-radius:6px;">
              ${['OPEN','IN_PROGRESS','RESOLVED'].map(s =>
                `<option value="${s}" ${t.status===s?'selected':''}>${s}</option>`).join('')}
            </select>
          </td>
        </tr>`).join('');
    } catch {}
  }

  // Activity Logs
  const tbodyLogs = document.getElementById('adminLogsBody');
  if (tbodyLogs) {
    try {
      const logs = await apiFetch('/api/admin/logs');
      tbodyLogs.innerHTML = logs.map(l => `
        <tr>
          <td style="color:var(--text-muted)">${new Date(l.timestamp).toLocaleString()}</td>
          <td style="font-weight:600">${l.adminUsername}</td>
          <td><span class="badge badge-${l.actionType.toLowerCase()}">${l.actionType}</span></td>
          <td>${l.entityName}</td>
          <td>${l.details}</td>
        </tr>`).join('');
    } catch {}
  }
}

async function adminAddBook(e) {
  e.preventDefault();
  const book = {
    title: document.getElementById('bTitle').value,
    author: document.getElementById('bAuthor').value,
    price: parseFloat(document.getElementById('bPrice').value),
    stock: parseInt(document.getElementById('bStock').value),
    };
  try {
    const created = await apiFetch('/api/books', { method:'POST', body:JSON.stringify(book) });
    books.push(created);
    document.getElementById('addBookForm').reset();
    showToast('Book added! ', 'success');
    loadAdmin();
  } catch {}
}

async function adminDeleteBook(id) {
  if (!confirm('Delete this book?')) return;
  try {
    await apiFetch(`/api/books/${id}`, { method: 'DELETE' });
    books = books.filter(b => b.id !== id);
    loadAdmin();
  } catch {}
}
async function updateOrderStatus(id, status) {
  try { await apiFetch(`/api/orders/${id}/status`, { method:'PUT', body:JSON.stringify({ status }) }); showToast('Updated ', 'success'); loadAdmin(); } catch {}
}

async function updateTicketStatus(id, status) {
  try { await apiFetch(`/api/support/${id}/status`, { method:'PUT', body:JSON.stringify({ status }) }); showToast('Updated ', 'success'); loadAdmin(); } catch {}
}

/* ============================================================
   TOAST
   ============================================================ */
let toastTimer;
function showToast(msg, type = '') {
  const t = document.getElementById('toast');
  if(!t) return;
  t.textContent = msg; t.className = 'toast show ' + type;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { t.classList.remove('show'); }, 3000);
}

/* ============================================================
   ADVANCED CATALOG PAGE LOGIC
   ============================================================ */
let searchTimeout;

async function applyAdvancedFilters() {
  const container = document.getElementById('advancedCatalogGrid');
  if (!container) return; // Not on the catalog page
  
  clearTimeout(searchTimeout);
  
  searchTimeout = setTimeout(async () => {
    container.innerHTML = '<div class="text-center w-100"><p class="text-muted">Searching books...</p></div>';
    
    try {
      const search = document.getElementById('advSearchInput')?.value || '';
      const cats = Array.from(document.querySelectorAll('.filter-cat:checked')).map(cb => cb.value).join(',');
      const lang = document.getElementById('filterLang')?.value || '';
      const minPrice = document.getElementById('filterMinPrice')?.value || '';
      const maxPrice = document.getElementById('filterMaxPrice')?.value || '';
      const minRating = document.getElementById('filterRating')?.value || '';
      const avail = document.querySelector('input[name="availability"]:checked')?.value || '';
      const sortBy = document.getElementById('sortDropdown')?.value || 'newest';
      
      const params = new URLSearchParams();
      if (search) params.append('search', search);
      if (cats) params.append('category', cats.split(',')[0]); 
      if (lang) params.append('language', lang);
      if (minPrice) params.append('minPrice', minPrice);
      if (maxPrice) params.append('maxPrice', maxPrice);
      if (minRating) params.append('minRating', minRating);
      if (avail) params.append('availability', avail);
      if (sortBy) params.append('sortBy', sortBy);
      
      const url = '/api/books?' + params.toString();
      books = await apiFetch(url);
      
      const countEl = document.getElementById('catalogResultCount');
      if (countEl) countEl.textContent = `Showing ${books.length} result(s)`;
      
      renderAdvancedBookGrid(books);
    } catch (e) {
      container.innerHTML = '<p class="text-muted text-center w-100">Failed to load books.</p>';
    }
  }, 300);
}

function clearFilters() {
  if (document.getElementById('advSearchInput')) document.getElementById('advSearchInput').value = '';
  document.querySelectorAll('.filter-cat').forEach(cb => cb.checked = false);
  if (document.getElementById('filterLang')) document.getElementById('filterLang').value = '';
  if (document.getElementById('filterMinPrice')) document.getElementById('filterMinPrice').value = '';
  if (document.getElementById('filterMaxPrice')) document.getElementById('filterMaxPrice').value = '';
  if (document.getElementById('filterRating')) document.getElementById('filterRating').value = '';
  if (document.querySelector('input[name="availability"][value=""]')) {
      document.querySelector('input[name="availability"][value=""]').checked = true;
  }
  if (document.getElementById('sortDropdown')) document.getElementById('sortDropdown').value = 'newest';
  
  applyAdvancedFilters();
}

function openSaveFilterModal() {
  document.getElementById('saveFilterNameInput').value = '';
  document.getElementById('saveFilterModal').classList.remove('hidden');
  document.getElementById('saveFilterNameInput').focus();
}

function closeSaveFilterModal() {
  document.getElementById('saveFilterModal').classList.add('hidden');
}

function showCustomToast(msg, type = 'success') {
  const toast = document.getElementById('toast');
  if (toast) {
    toast.textContent = msg;
    toast.style.background = type === 'error' ? 'rgba(239, 68, 68, 0.9)' : 'rgba(74, 222, 128, 0.9)';
    toast.style.color = '#fff';
    toast.style.display = 'block';
    toast.classList.add('show');
    setTimeout(() => {
      toast.classList.remove('show');
      toast.style.display = 'none';
    }, 3000);
  } else {
    alert(msg);
  }
}

async function submitSaveFilter() {
  const filterName = document.getElementById('saveFilterNameInput').value.trim();
  if (!filterName) {
      showCustomToast('Please enter a name for the filter.', 'error');
      return;
  }

  const search = document.getElementById('advSearchInput')?.value || '';
  const cats = Array.from(document.querySelectorAll('.filter-cat:checked')).map(cb => cb.value).join(',');
  const lang = document.getElementById('filterLang')?.value || '';
  const minPrice = document.getElementById('filterMinPrice')?.value || '';
  const maxPrice = document.getElementById('filterMaxPrice')?.value || '';
  const minRating = document.getElementById('filterRating')?.value || '';
  const avail = document.querySelector('input[name="availability"]:checked')?.value || '';

  const payload = {
    name: filterName,
    searchQuery: search,
    categories: cats,
    minPrice: minPrice ? parseFloat(minPrice) : null,
    maxPrice: maxPrice ? parseFloat(maxPrice) : null,
    language: lang,
    minRating: minRating ? parseFloat(minRating) : null,
    availability: avail
  };

  try {
    const res = await fetch(API + '/api/saved-filters', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      body: JSON.stringify(payload)
    });
    if(res.ok) {
        closeSaveFilterModal();
        showCustomToast('Filter saved successfully!', 'success');
    } else {
        showCustomToast('Failed to save filter.', 'error');
    }
  } catch (e) {
    showCustomToast('Failed to save filter: ' + e.message, 'error');
  }
}

function renderAdvancedBookGrid(bookList) {
  const grid = document.getElementById('advancedCatalogGrid');
  if (!grid) return;
  if (!bookList || bookList.length === 0) {
    grid.innerHTML = '<p class="text-muted" style="padding:2rem; text-align:center; width:100%; grid-column: 1 / -1;">No books found matching your criteria.</p>';
    return;
  }
  
  grid.innerHTML = bookList.map((b, i) => {
    const r = b.rating || 5.0;
    let stars = ''.repeat(Math.floor(r)) + ''.repeat(5 - Math.floor(r));
    if (r % 1 !== 0) stars = stars.replace('', ''); 
    
    return `
    <div class="book-card" style="animation-delay:${(i%10) * 0.05}s">
      <div class="book-cover-art" style="background:${coverGradient(i)}">${b.coverImage ? `<img src="/uploads/covers/${b.coverImage}" style="width:100%; height:100%; object-fit:cover; border-radius:inherit;"/>` : ''}</div>
      <h3>${b.title}</h3>
      <p class="author">by ${b.author}</p>
      
      <div style="display:flex; justify-content:space-between; margin:0.5rem 0;">
        <span class="genre-tag" style="margin:0;">${b.genre || 'Unknown'}</span>
        <span style="color:var(--primary); font-size:0.9rem;">${stars} ${r.toFixed(1)}</span>
      </div>
      
      <div class="price">Rs ${Number(b.price).toFixed(2)}</div>
      <div class="stock ${b.stock <= 5 ? 'stock-low' : ''}" style="margin-bottom:1rem;">
        ${b.stock > 0 ? ` In Stock (${b.stock})` : ' Out of stock'}
      </div>
      
      <div style="display:flex; flex-direction:column; gap:0.5rem;">
        <button class="btn-ghost btn-sm w-100" onclick="openBookDetailsModal(${b.id})" style="border: 1px solid var(--glass-b);">View Details</button>
        <div style="display:flex; gap:0.5rem;">
          ${new URLSearchParams(window.location.search).get('addToCollectionId') ? 
            `<button class="btn-primary btn-sm w-100" onclick="addToCollection(${b.id}, ${new URLSearchParams(window.location.search).get('addToCollectionId')}, event)">+ Add to Collection</button>` 
            : 
            `<button class="btn-primary btn-sm w-100" onclick="addToCart(${b.id}, event)" ${b.stock === 0 ? 'disabled' : ''}>Add to Cart </button>
             ${token ? `<button class="btn-secondary btn-sm" onclick="addToWishlist(${b.id}, event)" title="Add to Wishlist"></button>` : ''}`
          }
        </div>
      </div>
    </div>`;
  }).join('');
}

function openBookDetailsModal(id) {
  playPageTransition('book-details.html?id=' + id, false);
}

function closeBookDetailsModal() {
  document.getElementById('bookDetailsModal').classList.add('hidden');
}

/* ============================================================
   BOOK DETAILS PAGE LOGIC
   ============================================================ */
async function loadBookDetailsPage() {
  const urlParams = new URLSearchParams(window.location.search);
  const id = urlParams.get('id');
  if (!id) {
    document.getElementById('detailsLoading').classList.add('hidden');
    document.getElementById('detailsError').classList.remove('hidden');
    return;
  }
  
  try {
    const book = await apiFetch(`/api/books/${id}`);
    
    // We add it to the global array temporarily so addToCart/addToWishlist can find it if needed
    // But we already rewrote finishAddToCart to use the passed book, though find still runs. 
    // Wait, addToCart looks up by ID in the global `books` array. We must ensure it's there.
    if (!books.find(b => b.id === book.id)) books.push(book);

    let recent = JSON.parse(localStorage.getItem('recent_books') || '[]');
    recent = recent.filter(b => b.id !== book.id);
    recent.unshift({ id: book.id, title: book.title, author: book.author, price: book.price, coverImage: book.coverImage });
    if (recent.length > 10) recent = recent.slice(0, 10);
    localStorage.setItem('recent_books', JSON.stringify(recent));
    
    // Populate Hero
    document.getElementById('detailTitle').textContent = book.title;
    document.getElementById('detailCoverEmoji').innerHTML = book.coverImage ? `<img src='/uploads/covers/${book.coverImage}' style='width:100%; height:100%; object-fit:cover; border-radius:inherit;'/>` : '';
    document.getElementById('detailAuthor').textContent = book.author;
    document.getElementById('detailIsbn').textContent = book.isbn || '';
    document.getElementById('detailCategory').textContent = book.genre || 'Unknown';
    document.getElementById('detailPublisher').textContent = book.publisher || '';
    
    const r = book.rating || 5.0;
    let stars = ''.repeat(Math.floor(r)) + ''.repeat(5 - Math.floor(r));
    if (r % 1 !== 0) stars = stars.replace('', '');
    document.getElementById('detailRating').textContent = `${stars} ${r.toFixed(1)}`;
    document.getElementById('reviewAvgScore').textContent = r.toFixed(1);
    document.getElementById('reviewAvgStars').textContent = stars;
    
    document.getElementById('detailPrice').textContent = 'Rs ' + Number(book.price).toFixed(2);
    document.getElementById('detailAvailability').textContent = book.stock > 0 ? ` In Stock (${book.stock} copies)` : ' Out of Stock';
    document.getElementById('detailAvailability').style.color = book.stock > 0 ? 'var(--green)' : 'var(--danger, #ff4c4c)';
    
    const cartBtn = document.getElementById('detailAddToCartBtn');
    if(cartBtn) {
        cartBtn.disabled = (book.stock === 0);
        cartBtn.onclick = (e) => {
            const qty = parseInt(document.getElementById('detailQty').value) || 1;
            addToCart(book.id, e, qty);
        };
    }
    
    const wishBtn = document.getElementById('detailAddWishlistBtn');
    if (wishBtn) wishBtn.onclick = (e) => addToWishlist(book.id, e);
    
    // Populate Tabs
    document.getElementById('detailDescription').textContent = book.description || 'No description available.';
    document.getElementById('tabIsbn').textContent = book.isbn || '';
    document.getElementById('tabAuthor').textContent = book.author;
    document.getElementById('tabPublisher').textContent = book.publisher || '';
    document.getElementById('tabLanguage').textContent = book.language || 'English';
    document.getElementById('tabYear').textContent = book.publicationYear || '';
    document.getElementById('tabCategory').textContent = book.genre || '';
    
    document.getElementById('detailsLoading').classList.add('hidden');
    document.getElementById('detailsContent').classList.remove('hidden');
    
    // Load Related Books (same genre)
    loadRelatedBooks(book.genre, book.id);
    
  } catch (err) {
    document.getElementById('detailsLoading').classList.add('hidden');
    document.getElementById('detailsError').classList.remove('hidden');
  }
}

async function loadRelatedBooks(genre, excludeId) {
  try {
    let url = '/api/books';
    if (genre) url += `?category=${encodeURIComponent(genre)}&sortBy=rating_desc`;
    const allBooks = await apiFetch(url);
    const related = allBooks.filter(b => b.id !== excludeId).slice(0, 4);
    
    const grid = document.getElementById('relatedBooksGrid');
    if (!grid) return;
    
    if (related.length === 0) {
      grid.innerHTML = '<p class="text-muted">No related books found.</p>';
      return;
    }
    
    // Using existing renderAdvancedBookGrid logic basically
    grid.innerHTML = related.map((b, i) => {
      const r = b.rating || 5.0;
      let stars = ''.repeat(Math.floor(r)) + ''.repeat(5 - Math.floor(r));
      if (r % 1 !== 0) stars = stars.replace('', '');
      return `
      <div class="book-card">
        <div class="book-cover-art" style="background:${coverGradient(i)}">${b.coverImage ? `<img src="/uploads/covers/${b.coverImage}" style="width:100%; height:100%; object-fit:cover; border-radius:inherit;"/>` : ''}</div>
        <h3>${b.title}</h3>
        <p class="author">by ${b.author}</p>
        <div style="display:flex; justify-content:space-between; margin:0.5rem 0;">
          <span class="genre-tag" style="margin:0;">${b.genre || 'Unknown'}</span>
          <span style="color:var(--primary); font-size:0.9rem;">${stars} ${r.toFixed(1)}</span>
        </div>
        <div class="price">Rs ${Number(b.price).toFixed(2)}</div>
        <div style="display:flex; flex-direction:column; gap:0.5rem; margin-top:1rem;">
          <button class="btn-ghost btn-sm w-100" onclick="openBookDetailsModal(${b.id})" style="border: 1px solid var(--glass-b);">View Details</button>
          <button class="btn-primary btn-sm w-100" onclick="addToCart(${b.id}, event)" ${b.stock === 0 ? 'disabled' : ''}>Add to Cart </button>
        </div>
      </div>`;
    }).join('');
  } catch (e) {}
}

function updateDetailQty(delta) {
  const input = document.getElementById('detailQty');
  if(!input) return;
  let val = parseInt(input.value) + delta;
  if(val < 1) val = 1;
  input.value = val;
}

function switchTab(tabId) {
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  document.querySelectorAll('.tab-pane').forEach(p => p.classList.add('hidden', 'active')); // Reset
  document.querySelectorAll('.tab-pane').forEach(p => { p.classList.remove('active'); p.style.display = 'none'; });
  
  event.currentTarget.classList.add('active');
  const target = document.getElementById('tab-' + tabId);
  if(target) {
      target.classList.remove('hidden');
      target.classList.add('active');
      target.style.display = 'block';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  if (window.location.pathname.endsWith('book-details.html')) {
    loadBookDetailsPage();
  }
});

/* ============================================================
   ADMIN BOOK MANAGEMENT
   ============================================================ */

async function loadAdminBooks() {
  try {
    const list = await apiFetch('/api/books');
    window.adminBooks = list; // store globally for easy access
    
    const tbody = document.getElementById('adminBooksTableBody');
    if (!tbody) return;
    
    if (list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No books found.</td></tr>';
      return;
    }
    
    tbody.innerHTML = list.map(b => `
      <tr>
        <td>#${b.id}</td>
        <td style="font-size:2rem;">${b.coverImage ? `<img src="/uploads/covers/${b.coverImage}" style="width:100%; height:100%; object-fit:cover; border-radius:inherit;"/>` : ''}</td>
        <td><strong>${b.title}</strong><br><span class="text-muted">by ${b.author}</span></td>
        <td>${b.isbn || ''}<br><span class="genre-tag" style="margin-top:4px;">${b.genre}</span></td>
        <td style="font-weight:bold; color:var(--primary);">Rs ${Number(b.price).toFixed(2)}</td>
        <td>
          <span class="badge ${b.stock > 5 ? 'badge-success' : (b.stock > 0 ? 'badge-warning' : 'badge-danger')}">
            ${b.stock}
          </span>
        </td>
        <td>
          <div style="display:flex; gap:0.5rem; justify-content:center;">
            <button class="btn-ghost btn-sm" onclick="openBookModal(${b.id})">Edit</button>
            <button class="btn-ghost btn-sm text-pink" onclick="deleteBook(${b.id})">Delete</button>
          </div>
        </td>
      </tr>
    `).join('');
  } catch (e) {
    showToast('Failed to load books', 'error');
  }
}

function openBookModal(id) {
  const modal = document.getElementById('adminBookModal');
  const form = document.getElementById('adminBookForm');
  if (!modal || !form) return;
  
  form.reset();
  document.getElementById('formBookId').value = '';
  document.getElementById('modalTitle').textContent = 'Add New Book';
  // Set category to empty placeholder for new book
  const catSelect = document.getElementById('fCategory');
  if (catSelect) catSelect.value = '';
  
  if (id) {
    const book = window.adminBooks?.find(b => b.id === id);
    if (book) {
      document.getElementById('modalTitle').textContent = 'Edit Book';
      document.getElementById('formBookId').value = book.id;
      
      document.getElementById('fTitle').value = book.title;
      document.getElementById('fAuthor').value = book.author;
      document.getElementById('fIsbn').value = book.isbn || '';
      if (catSelect) catSelect.value = book.genre || '';
      document.getElementById('fPublisher').value = book.publisher || '';
      document.getElementById('fLanguage').value = book.language || '';
      document.getElementById('fYear').value = book.publicationYear || '';
      document.getElementById('fPrice').value = book.price;
      document.getElementById('fStock').value = book.stock;
      document.getElementById('fRating').value = book.rating || '';
      document.getElementById('fDescription').value = book.description || '';
    }
  }
  
  modal.classList.remove('hidden');
}

function closeBookModal() {
  const modal = document.getElementById('adminBookModal');
  if (modal) modal.classList.add('hidden');
}

async function saveBook(e) {
  e.preventDefault();
  
  const id = document.getElementById('formBookId').value;
  const book = {
    title: document.getElementById('fTitle').value,
    author: document.getElementById('fAuthor').value,
    isbn: document.getElementById('fIsbn').value,
    genre: document.getElementById('fCategory').value,
    publisher: document.getElementById('fPublisher').value,
    language: document.getElementById('fLanguage').value,
    publicationYear: parseInt(document.getElementById('fYear').value) || null,
    price: parseFloat(document.getElementById('fPrice').value),
    stock: parseInt(document.getElementById('fStock').value),
    rating: parseFloat(document.getElementById('fRating').value) || null,
    coverEmoji: document.getElementById('fEmoji').value,
    description: document.getElementById('fDescription').value
  };
  
  try {
    if (id) {
      await apiFetch(`/api/admin/books/${id}`, {
        method: 'PUT',
        body: JSON.stringify(book)
      });
      showToast('Book updated successfully', 'success');
    } else {
      await apiFetch('/api/admin/books', {
        method: 'POST',
        body: JSON.stringify(book)
      });
      showToast('Book added successfully', 'success');
    }
    closeBookModal();
    loadAdminBooks();
  } catch (err) {
    showToast('Failed to save book', 'error');
  }
}

async function deleteBook(id) {
  if (!confirm('Are you sure you want to delete this book? This action cannot be undone.')) return;
  
  try {
    await apiFetch(`/api/admin/books/${id}`, { method: 'DELETE' });
    showToast('Book deleted successfully', 'success');
    loadAdminBooks();
  } catch (err) {
    showToast('Failed to delete book', 'error');
  }
}

/* ============================================================
   SUPPORT MODULE
   ============================================================ */

async function loadSupportTickets() {
  const listEl = document.getElementById('myTicketsList');
  if (!listEl) return;

  try {
    const tickets = await apiFetch('/api/support');
    if (!tickets || tickets.length === 0) {
      listEl.innerHTML = '<p class="text-muted">You have no support tickets yet.</p>';
      return;
    }

    listEl.innerHTML = tickets.map((t, idx) => `
      <div class="ticket-card glass-panel" style="animation: fade-in-up 0.3s ease forwards; animation-delay: ${idx * 0.05}s; display: flex; justify-content: space-between; align-items: center;" onclick="viewTicketDetails(${t.id})">
        <div>
          <div style="font-weight: 700; color: var(--gold); margin-bottom: 0.3rem;">Ticket #T${String(t.id).padStart(3, '0')}</div>
          <div style="font-size: 0.9rem; color: #ccc; margin-bottom: 0.5rem;">Category: ${t.issueCategory}</div>
          <div style="font-size: 0.8rem; color: #888;">Created: ${new Date(t.createdDate).toLocaleDateString()}</div>
        </div>
        <div style="text-align: right;">
          <span class="status-badge status-${t.status}">${t.status}</span>
          <div style="margin-top: 1rem;"><span class="btn-ghost btn-sm">View Response</span></div>
        </div>
      </div>
    `).join('');
  } catch (err) {
    listEl.innerHTML = '<p class="text-muted">Error loading tickets.</p>';
  }
}

async function createTicket(event) {
  event.preventDefault();
  const btn = document.getElementById('submitTicketBtn');
  btn.innerHTML = '<span class="loading-spinner"></span> Submitting...';
  btn.disabled = true;

  const orderNum = document.getElementById('ticketOrderNum').value;
  const category = document.getElementById('ticketCategory').value;
  const desc = document.getElementById('ticketDesc').value;

  try {
    await apiFetch('/api/support', {
      method: 'POST',
      body: JSON.stringify({
        orderId: orderNum,
        issueCategory: category,
        description: desc
      })
    });
    
    showToast('Support ticket created successfully!', 'success');
    document.getElementById('createTicketForm').reset();
    loadSupportTickets();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.innerHTML = 'Submit Ticket';
    btn.disabled = false;
  }
}

async function viewTicketDetails(ticketId) {
  const modal = document.getElementById('ticketModal');
  const meta = document.getElementById('modalTicketMeta');
  const messagesDiv = document.getElementById('modalTicketMessages');
  
  if (!modal || !meta || !messagesDiv) return;

  try {
    const ticket = await apiFetch(`/api/support/${ticketId}`);
    
    document.getElementById('modalTicketTitle').textContent = `Ticket #T${String(ticket.id).padStart(3, '0')}`;
    
    meta.innerHTML = `
      <div><strong>Status:</strong> <span class="status-badge status-${ticket.status}">${ticket.status}</span></div>
      <div><strong>Category:</strong> ${ticket.issueCategory}</div>
      ${ticket.orderId ? `<div><strong>Order Number:</strong> #${ticket.orderId}</div>` : ''}
      <div><strong>Created:</strong> ${new Date(ticket.createdDate).toLocaleString()}</div>
      <div><strong>Last Updated:</strong> ${new Date(ticket.updatedDate).toLocaleString()}</div>
    `;

    messagesDiv.innerHTML = ticket.messages.map(msg => `
      <div class="chat-bubble ${msg.senderType.toLowerCase()}">
        <div style="font-size: 0.8rem; font-weight: 700; margin-bottom: 0.3rem; opacity: 0.7;">
          ${msg.senderType === 'CUSTOMER' ? 'You' : 'Support Team'}  ${new Date(msg.createdDate).toLocaleString()}
        </div>
        <div style="line-height: 1.4;">${msg.message}</div>
      </div>
    `).join('');

    modal.classList.remove('hidden');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

/* ============================================================
   NOTIFICATION LOGIC
   ============================================================ */

async function updateNotificationCount() {
  if (!token) return;
  try {
    const res = await fetch(API + '/notifications/unread-count', {
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (res.ok) {
      const data = await res.json();
      const badge = document.getElementById('notificationBadge');
      if (badge) {
        badge.textContent = data.count;
        if (data.count > 0) {
          badge.style.display = 'inline-block';
          badge.style.boxShadow = '0 0 10px var(--gold)'; // Soft golden glow
        } else {
          badge.style.display = 'none';
        }
      }
    }
  } catch (e) {
    console.error('Failed to update notification count', e);
  }
}

async function toggleNotificationDropdown(e) {
  e.preventDefault();
  const dropdown = document.getElementById('notificationDropdownContent');
  if (!dropdown) return;
  
  if (dropdown.style.display === 'block') {
    dropdown.style.display = 'none';
    return;
  }
  
  dropdown.style.display = 'block';
  dropdown.innerHTML = '<p class="text-muted" style="text-align:center; padding:1rem;">Loading...</p>';
  
  try {
    const res = await fetch(API + '/notifications', {
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (!res.ok) throw new Error('Failed to fetch notifications');
    const notifs = await res.json();
    
    if (notifs.length === 0) {
      dropdown.innerHTML = '<p class="text-muted" style="text-align:center; padding:1rem;">No new notifications</p>';
      return;
    }
    
    // Show up to 5 latest
    const latest = notifs.slice(0, 5);
    
    let html = '<div style="max-height: 300px; overflow-y: auto;">';
    latest.forEach(n => {
      const time = new Date(n.createdDate).toLocaleString();
      const bg = n.read ? 'transparent' : 'rgba(220,165,74,0.1)';
      html += `
        <div style="padding: 0.8rem; border-bottom: 1px solid var(--glass-b); background: ${bg}; cursor:pointer;" onclick="window.location.href='notifications.html'">
          <strong style="display:block; color: ${n.read ? 'var(--text)' : 'var(--gold)'}; margin-bottom: 0.2rem;">${n.title}</strong>
          <p style="font-size: 0.85rem; color: #ccc; margin: 0 0 0.4rem 0;">${n.message}</p>
          <small style="color: #777;">${time}</small>
        </div>
      `;
    });
    html += '</div>';
    html += '<div style="text-align:center; padding-top: 0.8rem; border-top: 1px solid var(--glass-b);"><a href="notifications.html" style="color: var(--gold); text-decoration:none; font-size:0.9rem;">View All Notifications</a></div>';
    
    dropdown.innerHTML = html;
  } catch (err) {
    dropdown.innerHTML = '<p class="text-muted" style="text-align:center; padding:1rem;">Error loading</p>';
  }
}

// Used on notifications.html
async function loadNotificationsPage() {
  const container = document.getElementById('notificationsPageList');
  if (!container) return;
  
  try {
    const res = await fetch(API + '/notifications', {
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (!res.ok) throw new Error('Failed to fetch');
    const notifs = await res.json();
    
    if (notifs.length === 0) {
      container.innerHTML = '<div class="text-center fade-in-up" style="padding:3rem;"><p class="text-muted">You have no notifications yet.</p></div>';
      return;
    }
    
    container.innerHTML = notifs.map((n, i) => `
      <div class="notification-card ${n.read ? '' : 'unread'}" style="animation-delay: ${i * 0.05}s">
        <div class="notification-content">
          <div class="notification-title">${n.title}</div>
          <div class="notification-message">${n.message}</div>
          <div class="notification-date">${new Date(n.createdDate).toLocaleString()}</div>
        </div>
        <div class="notification-actions">
          ${!n.read ? `<button class="btn-primary btn-sm" onclick="markNotificationRead(${n.id}, this)">Mark as Read</button>` : `<span class="badge" style="background:rgba(255,255,255,0.1); color:#aaa; font-weight:normal; padding:4px 8px; font-size:0.8rem;">Read</span>`}
          <button class="btn-ghost btn-sm" onclick="deleteNotification(${n.id}, this)" style="color:#ff6b6b; border-color: rgba(255,107,107,0.2);">Delete</button>
        </div>
      </div>
    `).join('');
    
  } catch (err) {
    container.innerHTML = '<p class="text-muted text-center">Failed to load notifications</p>';
  }
}

async function markNotificationRead(id, btnEl) {
  try {
    const res = await fetch(API + '/notifications/' + id + '/read', {
      method: 'PUT',
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (res.ok) {
      // Update UI
      const card = btnEl.closest('.notification-card');
      card.classList.remove('unread');
      btnEl.outerHTML = '<span class="badge" style="background:rgba(255,255,255,0.1); color:#aaa; font-weight:normal; padding:4px 8px; font-size:0.8rem;">Read</span>';
      updateNotificationCount();
    }
  } catch (e) {
    showToast('Failed to mark read', 'error');
  }
}

async function deleteNotification(id, btnEl) {
  if (!confirm('Delete this notification?')) return;
  try {
    const res = await fetch(API + '/notifications/' + id, {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (res.ok) {
      const card = btnEl.closest('.notification-card');
      card.style.transform = 'translateY(-20px)';
      card.style.opacity = '0';
      setTimeout(() => { card.remove(); updateNotificationCount(); }, 300);
      showToast('Notification deleted', 'success');
    }
  } catch (e) {
    showToast('Failed to delete', 'error');
  }
}

/* ============================================================
   WELCOME WIDGET
   ============================================================ */
function initWelcomeWidget() {
  const combinedWidget = document.getElementById('wwCombinedWidget');
  const quoteWidget = document.getElementById('wwQuoteWidget');
  
  if (!combinedWidget || !quoteWidget) return;
  
  if (!token || !currentUser) {
    combinedWidget.style.display = 'none';
    quoteWidget.style.display = 'none';
    return;
  }
  
  combinedWidget.style.display = 'flex';
  quoteWidget.style.display = 'block';
  
  const nameEl = document.getElementById('wwCombinedName');
  const greetingEl = document.getElementById('wwCombinedGreeting');
  const dateEl = document.getElementById('wwCombinedDate');
  const timeEl = document.getElementById('wwCombinedTime');
  const iconEl = document.getElementById('wwCombinedIcon');
  
  const displayName = currentUser.username; 
  if (nameEl) nameEl.textContent = displayName;
  
  // Update Time and Greeting continuously
  function updateTime() {
    const now = new Date();
    const hours = now.getHours();
    
    // Greeting & Icon Logic
    let greeting = '';
    let iconClass = 'fa-moon';
    
    if (hours >= 5 && hours < 12) {
      greeting = 'Good Morning...!!';
      iconClass = 'fa-sun';
    } else if (hours >= 12 && hours < 18) {
      greeting = 'Good Afternoon...!!';
      iconClass = 'fa-sun';
    } else if (hours >= 18 && hours <= 23) {
      greeting = 'Good Evening...!!';
      iconClass = 'fa-moon';
    } else {
      greeting = 'Good Night...!!';
      iconClass = 'fa-moon';
    }
    
    if (greetingEl) greetingEl.textContent = greeting;
    if (iconEl) iconEl.className = a-solid  + iconClass;
    
    // Time formatting
    const days = ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
    const months = ['January','February','March','April','May','June','July','August','September','October','November','December'];
    
    const dayName = days[now.getDay()];
    const monthName = months[now.getMonth()];
    const dateNum = now.getDate();
    
    let calcHours = hours % 12;
    calcHours = calcHours ? calcHours : 12;
    const ampm = hours >= 12 ? 'PM' : 'AM';
    const mins = now.getMinutes().toString().padStart(2, '0');
    
    if (dateEl) dateEl.textContent = dayName + ', ' + monthName + ' ' + dateNum;
    if (timeEl) timeEl.textContent = calcHours.toString().padStart(2, '0') + ':' + mins + ' ' + ampm;
  }
  
  updateTime();
  setInterval(updateTime, 1000);
}



/* ============================================================
   CHATBOT LOGIC
   ============================================================ */
function toggleChat() {
  const window = document.getElementById('chat-window');
  if (window.style.display === 'none' || window.style.display === '') {
    window.style.display = 'flex';
  } else {
    window.style.display = 'none';
  }
}

function handleChatEnter(event) {
  if (event.key === 'Enter') {
    sendChatMessage();
  }
}

function getBotResponse(msg) {
  const text = msg.toLowerCase().trim();
  
  // Greetings
  if (text.match(/\b(hi|hello|hey|greetings|morning|afternoon|evening)\b/)) {
    return "Hello there! How can I help you find your next great read? 📖";
  }
  // Gratitude
  if (text.match(/\b(thanks|thank you|thx|appreciate)\b/)) {
    return "You're very welcome! Let me know if you need anything else.";
  }
  // Identity
  if (text.match(/\b(who are you|your name|what are you)\b/)) {
    return "I'm the Starlight Assistant, your virtual bookstore helper!";
  }
  // How are you
  if (text.match(/\b(how are you|how do you do|how's it going)\b/)) {
    return "I'm just a simple bot, but I'm having a great day helping you with books!";
  }
  // Books/Catalog
  if (text.match(/\b(book|books|novel|story|catalog|read)\b/) && text.match(/\b(looking for|find|search|recommend|want|where)\b/)) {
    return "You can explore all our books in the 'Catalog' section at the top of the page. We have everything from Sci-Fi to History!";
  }
  // Delivery / Shipping
  if (text.match(/\b(shipping|delivery|deliver|ship|arrive|how long)\b/)) {
    return "We usually deliver within 3-5 business days. You'll receive a notification when your order is shipped!";
  }
  // Restock / Availability
  if (text.match(/\b(restock|out of stock|empty|when|available)\b/)) {
    return "We typically restock out-of-stock items within 1-2 weeks! Keep an eye on our catalog. ✨";
  }
  // Price / Cost
  if (text.match(/\b(price|cost|how much|expensive|cheap|money)\b/)) {
    return "Our prices vary by book, but you can see all pricing directly in the Catalog! We accept Cash on Delivery and Card payments.";
  }
  // Orders / Tracking
  if (text.match(/\b(order|orders|track|where is my|status)\b/)) {
    return "You can easily track and view all your orders in the 'My Orders' section from your account menu.";
  }
  // Returns & Order Issues
  if (text.match(/\b(return|cancel|broken|damage)\b/)) {
    return "If you need assistance with an order, please open a support ticket in your 'Support Tickets' dashboard and our team will help you.";
  }
  // Contact / Human
  if (text.match(/\b(human|person|contact|support|help|manager|owner)\b/)) {
    return "If you need human assistance, head over to 'Get Help' or 'Support Tickets' to speak directly with our team!";
  }

  // Generic Question Handlers
  if (text.startsWith("what ")) {
    return "That's a good question! While I focus mainly on books and orders, our support team can give you a more detailed answer.";
  }
  if (text.startsWith("how ")) {
    return "I might not know exactly how, but if it's about our bookstore, the Catalog and Account menus usually have the answer!";
  }
  if (text.startsWith("why ")) {
    return "I suppose some things are just the way the universe wrote them! 🌌 But for store issues, support can help.";
  }
  if (text.startsWith("can you ") || text.startsWith("could you ")) {
    return "I can certainly try! My main skills are helping you navigate the store, check orders, and find books.";
  }
  if (text.startsWith("where ")) {
    return "You can find most things right here on our website. Use the navigation bar at the top to explore!";
  }
  if (text.includes("?")) { // Any other question
    return "Hmm, I'm not entirely sure about that. But if you browse around, you might find what you're looking for!";
  }

  // Fallback
  const fallbacks = [
    "Aah I need an update... I'm still learning! Please contact our support team for more complex queries.",
    "I'm just a simple bot, so I might not understand that completely. How about exploring our book catalog?",
    "That's interesting! Tell me more, or ask me something about books, orders, or deliveries."
  ];
  return fallbacks[Math.floor(Math.random() * fallbacks.length)];
}

function sendChatMessage() {
  const inputEl = document.getElementById('chat-input');
  const msg = inputEl.value.trim();
  if (!msg) return;
  
  const messagesContainer = document.getElementById('chat-messages');
  
  // Add User Message
  const userMsgDiv = document.createElement('div');
  userMsgDiv.className = 'chat-msg user-msg';
  userMsgDiv.textContent = msg;
  messagesContainer.appendChild(userMsgDiv);
  
  inputEl.value = '';
  messagesContainer.scrollTop = messagesContainer.scrollHeight;
  
  // Process Bot Response
  setTimeout(() => {
    const response = getBotResponse(msg);
    
    const botMsgDiv = document.createElement('div');
    botMsgDiv.className = 'chat-msg bot-msg';
    botMsgDiv.textContent = response;
    messagesContainer.appendChild(botMsgDiv);
    
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
  }, 600);
}



async function followAuthorFromDetails() { const author = document.getElementById('detailAuthor').textContent; if (!author) return; try { const res = await fetch(API + '/api/followed-authors', { method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token }, body: JSON.stringify({ authorName: author }) }); if (res.ok) { showCustomToast('Successfully followed ' + author, 'success'); } else { showCustomToast('Failed to follow author', 'error'); } } catch (err) { showCustomToast('Error: ' + err.message, 'error'); } }

async function addToCollection(bookId, collectionId, event) { if(event) event.stopPropagation(); try { const res = await fetch(API + '/api/collections/' + collectionId + '/add/' + bookId, { method: 'POST', headers: { 'Authorization': 'Bearer ' + token } }); if (res.ok) { showCustomToast('Added to collection!', 'success'); } else { showCustomToast('Failed to add to collection', 'error'); } } catch (e) { showCustomToast(e.message, 'error'); } }



function closeSelectCollectionModal() {
  document.getElementById('selectCollectionModal').classList.add('hidden');
}

async function promptAddCollectionFromDetails() {
  try {
    const res = await fetch(API + '/api/collections', { headers: { 'Authorization': 'Bearer ' + token } });
    const collections = await res.json();
    if(collections.length === 0) {
      showCustomToast('You have no collections. Create one from the Personalized Features page.', 'info');
      return;
    }
    const list = document.getElementById('collectionSelectionList');
    list.innerHTML = collections.map(c => `
      <li style="border-bottom: 1px solid rgba(255,255,255,0.1); padding: 0.8rem 0; display: flex; justify-content: space-between; align-items: center;">
        <span>${c.name}</span>
        <button class="btn-primary btn-sm" onclick="addBookToSelectedCollection(${c.id})">Add</button>
      </li>
    `).join('');
    document.getElementById('selectCollectionModal').classList.remove('hidden');
  } catch(e) {
    showCustomToast(e.message, 'error');
  }
}

function addBookToSelectedCollection(colId) {
  const urlParams = new URLSearchParams(window.location.search);
  const bookId = urlParams.get('id');
  if (bookId) {
    addToCollection(bookId, colId, null);
    closeSelectCollectionModal();
  }
}

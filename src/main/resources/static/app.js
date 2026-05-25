const API_URL = "http://localhost:8080/api";

function switchTab(tab) {
    document.getElementById('login-form').style.display = tab === 'login' ? 'block' : 'none';
    document.getElementById('register-form').style.display = tab === 'register' ? 'block' : 'none';
    
    const btns = document.querySelectorAll('.tab-btn');
    btns[0].classList.toggle('active', tab === 'login');
    btns[1].classList.toggle('active', tab === 'register');
}

function showMessage(elementId, text, isError) {
    const el = document.getElementById(elementId);
    el.textContent = text;
    el.className = 'message ' + (isError ? 'error' : 'success');
}

// Login
document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    try {
        const res = await fetch(`${API_URL}/auth/signin`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();

        if (res.ok) {
            localStorage.setItem('user', JSON.stringify(data));
            // Feedback claro de éxito + redirect automático a home
            showMessage('login-message', '✓ Inicio de sesión exitoso. Redirigiendo...', false);
            document.getElementById('login-form').reset();
            checkAuth();
            setTimeout(() => {
                showSection('home-section');
                document.getElementById('login-message').textContent = '';
                // Scroll suave al tope para que se vea el navbar actualizado
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }, 900);
        } else {
            showMessage('login-message', data.message || 'Error de autenticación', true);
        }
    } catch (err) {
        showMessage('login-message', 'Error de conexión con el servidor', true);
    }
});

// Booking function removed from here to the bottom

// ==========================================
// Appointments Loaders
// ==========================================

async function loadAllAppointments() {
    const list = document.getElementById('admin-appointments-list');
    if (!list) return;
    
    list.innerHTML = '<p>Cargando citas...</p>';
    const user = JSON.parse(localStorage.getItem('user'));
    
    try {
        const res = await fetch(`${API_URL}/appointments/all`, {
            headers: { 'Authorization': 'Bearer ' + user.accessToken }
        });
        if (res.ok) {
            const apps = await res.json();
            list.innerHTML = '';
            if(apps.length === 0) {
                list.innerHTML = '<p style="color:var(--text-light)">No hay citas agendadas aún.</p>';
                return;
            }
            apps.forEach(a => {
                const date = new Date(a.appointmentTime).toLocaleString();
                list.innerHTML += `
                    <div style="background: rgba(255,255,255,0.7); padding: 15px; border-radius: 8px; border-left: 4px solid var(--primary-color);">
                        <p style="margin-bottom: 5px; font-weight: bold;">${a.serviceName}</p>
                        <p style="font-size: 0.9em; margin-bottom: 3px;"><strong>Cliente:</strong> ${a.clientName}</p>
                        <p style="font-size: 0.9em; margin-bottom: 3px;"><strong>Terapeuta:</strong> ${a.therapistName}</p>
                        <p style="font-size: 0.85em; color: var(--text-light);">📅 ${date}</p>
                    </div>
                `;
            });
        }
    } catch (err) {
        list.innerHTML = '<p>Error cargando citas globales.</p>';
    }
}

// NOTA: loadTherapistAppointments tenía dos definiciones en este archivo.
// La segunda (más abajo, ~línea 256) override a la primera y es la canónica.
// Eliminada la duplicada para evitar confusión al leer el código.

async function loadClientAppointments() {
    const list = document.getElementById('client-appointments-list');
    if (!list) return;
    
    list.innerHTML = '<p>Cargando citas...</p>';
    const user = JSON.parse(localStorage.getItem('user'));
    
    try {
        const res = await fetch(`${API_URL}/appointments/client`, {
            headers: { 'Authorization': 'Bearer ' + user.accessToken }
        });
        if (res.ok) {
            const apps = await res.json();
            list.innerHTML = '';
            if(apps.length === 0) {
                list.innerHTML = '<p style="color:var(--text-light)">No has agendado ninguna cita aún.</p>';
                return;
            }
            apps.forEach(a => {
                const date = new Date(a.appointmentTime).toLocaleString();
                const isCancelled = a.status === 'CANCELADA' || a.status === 'CANCELLED' || a.status === 'CANCELED';
                const statusColor = isCancelled ? '#999' : 'var(--primary-color)';
                const cancelBtn = isCancelled
                    ? ''
                    : `<button class="btn primary" style="background:#e74c3c; padding:5px 12px; font-size:0.85em; margin-top:10px;" onclick="cancelMyAppointment(${a.id})">Cancelar</button>`;
                list.innerHTML += `
                    <div class="service-card" style="padding: 15px; border-top: 3px solid var(--secondary-color); ${isCancelled ? 'opacity:0.6;' : ''}">
                        <h4 style="margin-bottom: 10px; color: var(--secondary-color);">${a.serviceName}</h4>
                        <p style="font-size: 0.9em; margin-bottom: 5px;"><strong>Terapeuta:</strong> ${a.therapistName}</p>
                        <p style="font-size: 0.85em; color: var(--text-dark);">📅 ${date}</p>
                        <p style="font-size: 0.8em; margin-top: 5px; color: ${statusColor};"><strong>Estado:</strong> ${a.status}</p>
                        ${cancelBtn}
                    </div>
                `;
            });
        }
    } catch (err) {
        list.innerHTML = '<p>Error cargando tus citas.</p>';
    }
}

// Register
document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('reg-name').value;
    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    // SEGURIDAD: el registro público siempre crea un CLIENTE (forzado en backend).
    // Roles ADMIN/THERAPIST se gestionan vía DataSeeder o panel admin existente.

    const submitBtn = e.target.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Creando...';

    try {
        const res = await fetch(`${API_URL}/auth/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, username, email, password })
        });
        const data = await res.json();

        if (res.ok) {
            showMessage('register-message', data.message, false);
            setTimeout(() => switchTab('login'), 2000);
        } else {
            showMessage('register-message', data.message || 'Error de registro', true);
        }
    } catch (err) {
        showMessage('register-message', 'Error de conexión con el servidor', true);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Crear Cuenta';
    }
});

// Initialization
document.addEventListener('DOMContentLoaded', () => {
    loadPublicServices();
});

// Routing & SPA Logic
function showSection(sectionId) {
    // Auth guard: si la seccion es protegida y no hay sesion, redirigir a login.
    // Esto cubre intentos via consola, links directos, o navegacion accidental.
    const PROTECTED = ['profile-container', 'dashboard-container'];
    if (PROTECTED.includes(sectionId) && !localStorage.getItem('user')) {
        sectionId = 'auth-container';
    }
    document.querySelectorAll('.content-section').forEach(sec => {
        sec.style.display = 'none';
        sec.classList.remove('active');
    });
    const section = document.getElementById(sectionId);
    if (section) {
        section.style.display = 'block';
        setTimeout(() => section.classList.add('active'), 10);
    }
}

function handleBookClick() {
    const userData = localStorage.getItem('user');
    if (userData) {
        // Logged in, show booking panel
        showSection('dashboard-container');
        loadServices();
    } else {
        // Not logged in, show auth
        showSection('auth-container');
        switchTab('login');
        showMessage('login-message', t('auth.login.title') + " para agendar", false);
    }
}

function showProfile() {
    const userData = localStorage.getItem('user');
    if (userData) {
        showSection('profile-container');
        checkAuth(); // Ensure correct sub-panels are shown
    } else {
        showSection('auth-container');
        switchTab('login');
    }
}

function logout() {
    localStorage.removeItem('user');
    checkAuth();
    // Limpiar el back-history para que el boton "atras" del navegador
    // no devuelva al usuario a una pantalla autenticada cacheada.
    // Aplicamos pushState para insertar una entrada nueva y luego
    // replaceState para evitar acumular history al hacer logout repetido.
    try {
        history.replaceState(null, '', window.location.pathname);
    } catch (_) { /* en algunos browsers history.replaceState puede fallar en file:// */ }
    showSection('home-section');
}

// Secciones que requieren sesion activa. Si el usuario llega a ellas sin token
// (por back-button, link directo, o bfcache restore), se redirige a login.
const PROTECTED_SECTIONS = ['profile-container', 'dashboard-container'];

// Guard ante back/forward del navegador: revalidar auth en cada cambio de history.
window.addEventListener('popstate', () => {
    const visibleSection = document.querySelector('.content-section.active');
    if (visibleSection && PROTECTED_SECTIONS.includes(visibleSection.id) && !localStorage.getItem('user')) {
        showSection('auth-container');
        switchTab('login');
    }
});

// Guard ante back-forward cache (bfcache): cuando el navegador restaura una pagina
// desde su cache de paginas previas (event.persisted=true), volvemos a validar el
// estado de auth para que el DOM cacheado no quede mostrando datos sensibles.
window.addEventListener('pageshow', (event) => {
    if (event.persisted) {
        checkAuth();
        const userData = localStorage.getItem('user');
        if (!userData) {
            showSection('home-section');
        }
    }
});

function checkAuth() {
    const userData = localStorage.getItem('user');
    const navLoginLi = document.getElementById('nav-login-li');
    const navLogoutLi = document.getElementById('nav-logout-li');
    const navProfileLi = document.getElementById('nav-profile-li');

    if (userData) {
        const user = JSON.parse(userData);
        document.getElementById('user-name-display').textContent = user.username;
        
        // Update Navbar
        if(navLoginLi) navLoginLi.style.display = 'none';
        if(navLogoutLi) navLogoutLi.style.display = 'block';
        if(navProfileLi) navProfileLi.style.display = 'block';
        
        // Hide all panels inside profile first
        document.getElementById('admin-panel').style.display = 'none';
        document.getElementById('therapist-panel').style.display = 'none';
        document.getElementById('client-panel').style.display = 'none';
        
        if (user.roles.includes('ROLE_ADMIN')) {
            // Admin ve TODOS sus paneles. Como ADMIN tambien puede agendar citas
            // como cliente para si mismo, mostramos client-panel ademas del admin-panel
            // y disparamos loadClientAppointments para que sus propias citas aparezcan
            // en "Mis Citas Agendadas".
            document.getElementById('admin-panel').style.display = 'block';
            document.getElementById('client-panel').style.display = 'block';
            loadAdminServices();
            loadAllAppointments();
            loadClientAppointments();
            loadUsersForRoleManagement();
        } else if (user.roles.includes('ROLE_THERAPIST')) {
            document.getElementById('therapist-panel').style.display = 'block';
            loadTherapistAppointments();
        } else {
            document.getElementById('client-panel').style.display = 'block';
            loadClientAppointments();
        }
    } else {
        // Update Navbar
        if(navLoginLi) navLoginLi.style.display = 'block';
        if(navLogoutLi) navLogoutLi.style.display = 'none';
        if(navProfileLi) navProfileLi.style.display = 'none';
    }
}

// Load Therapist Appointments
async function loadTherapistAppointments() {
    const userData = JSON.parse(localStorage.getItem('user'));
    try {
        const res = await fetch(`${API_URL}/appointments/therapist`, {
            headers: { 'Authorization': `Bearer ${userData.accessToken}` }
        });
        
        const list = document.getElementById('therapist-appointments-list');
        if (!list) return;
        
        list.innerHTML = '';
        if (res.ok) {
            const appointments = await res.json();
            if (appointments.length === 0) {
                list.innerHTML = `<p>${t('dash.therapist.empty')}</p>`;
                return;
            }
            appointments.forEach(a => {
                const dateObj = new Date(a.appointmentTime);
                list.innerHTML += `
                    <div class="service-card">
                        <h4>${a.serviceName}</h4>
                        <p><strong>Cliente:</strong> ${a.clientName}</p>
                        <p><strong>Fecha y Hora:</strong> ${dateObj.toLocaleString()}</p>
                        <p><strong>Estado:</strong> <span style="color: var(--primary-color)">${a.status}</span></p>
                    </div>
                `;
            });
        }
    } catch (err) {
        console.error(err);
    }
}

// Load Services
async function loadServices() {
    try {
        const userData = JSON.parse(localStorage.getItem('user'));
        const res = await fetch(`${API_URL}/services`, {
            headers: {
                'Authorization': `Bearer ${userData.accessToken}`
            }
        });
        const services = await res.json();
        
        const masajesList = document.getElementById('services-masajes-list');
        const facialesList = document.getElementById('services-faciales-list');
        const corporalesList = document.getElementById('services-corporales-list');
        
        if (masajesList) masajesList.innerHTML = '';
        if (facialesList) facialesList.innerHTML = '';
        if (corporalesList) corporalesList.innerHTML = '';

        services.forEach(s => {
            let cardHtml = `
                <div class="service-card" style="padding: 20px; text-align:left; display:flex; flex-direction:column; justify-content:space-between; height: 100%;">
                    <div>
                        <h4 style="margin-bottom:10px; font-size:1.1em; border-bottom: none; display:block;">${s.name}</h4>
                        <p style="font-size:0.9em; margin-bottom:15px; color:var(--text-color); line-height: 1.4;">${s.description || ''}</p>
                    </div>
                    <div style="margin-top: auto;">
                        <p class="price" style="margin-bottom:15px; font-size: 1.2em;">$${s.price}</p>
                        <button class="btn primary" style="width:100%;" onclick="openBookingModal(${s.id}, '${s.name.replace(/'/g, "\\'")}')">Agendar Cita</button>
                    </div>
                </div>
            `;
            
            const cat = (s.category || '').toUpperCase();
            if (cat === 'MASAJES RELAJANTES' && masajesList) {
                masajesList.innerHTML += cardHtml;
            } else if (cat === 'TRATAMIENTOS FACIALES' && facialesList) {
                facialesList.innerHTML += cardHtml;
            } else if (cat === 'TERAPIAS CORPORALES' && corporalesList) {
                corporalesList.innerHTML += cardHtml;
            } else if (masajesList) {
                masajesList.innerHTML += cardHtml; // fallback
            }
        });
    } catch (err) {
        console.error(err);
    }
}


async function loadAdminServices() {
    try {
        const userData = JSON.parse(localStorage.getItem('user'));
        const res = await fetch(`${API_URL}/services`, {
            headers: { 'Authorization': `Bearer ${userData.accessToken}` }
        });
        const services = await res.json();
        const list = document.getElementById('admin-services-list');
        if(!list) return;
        list.innerHTML = '';
        services.forEach(s => {
            list.innerHTML += `
                <div style="background: rgba(255,255,255,0.05); padding: 15px; border-radius: 8px; margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <h4 style="margin-bottom: 5px;">${s.name} <span style="font-size: 0.8em; color: #333; font-weight: normal;">(${s.category || 'General'})</span></h4>
                        <p style="font-size: 0.9em; margin-bottom: 5px;">Precio: $${s.price}</p>
                    </div>
                    <div style="display:flex; gap:10px;">
                        <button class="btn primary" style="padding: 5px 15px; font-size: 0.85em;" onclick="openEditModal(${s.id}, '${(s.category||'').replace(/'/g, "\\'")}', '${s.name.replace(/'/g, "\\'")}', '${(s.description||'').replace(/[\r\n]+/g, " ").replace(/'/g, "\\'")}', ${s.price}, '${(s.imageUrl||'').replace(/'/g, "\\'")}')">Editar</button>
                        <button class="btn primary" style="background: #e74c3c; padding: 5px 15px; font-size: 0.85em;" onclick="deleteService(${s.id})">Borrar</button>
                    </div>
                </div>
            `;
        });
    } catch (err) {
        console.error(err);
    }
}

// Load Public Services dynamically from DB to stay in sync
async function loadPublicServices() {
    try {
        const res = await fetch(`${API_URL}/services`);
        if (!res.ok) return;
        const services = await res.json();
        
        const container = document.getElementById('public-services-container');
        if (!container) return;
        container.innerHTML = '';

        // Group services by category
        const categories = {};
        services.forEach(s => {
            if (!categories[s.category]) categories[s.category] = [];
            categories[s.category].push(s);
        });

        // Create the elegant big cards for each category
        for (const [catName, catServices] of Object.entries(categories)) {
            let servicesHtml = '';
            catServices.forEach((s, index) => {
                const borderStyle = index < catServices.length - 1 ? 'border-bottom: 1px solid rgba(255,255,255,0.2); margin-bottom: 25px; padding-bottom: 20px;' : '';
                servicesHtml += `
                    <div style="${borderStyle}">
                        <h4 style="color: var(--secondary-color); font-size: 1.2em; margin-bottom: 10px;">${s.name}</h4>
                        <p style="margin-bottom: 15px;">${s.description || ''}</p>
                        <button class="btn primary" onclick="handleBookClick()">Agendar por $${s.price}</button>
                    </div>
                `;
            });

            const cardHtml = `
                <div class="about-card">
                    <h3 style="font-size: 1.8em; margin-bottom: 25px; text-align: center; border: none;">${catName}</h3>
                    ${servicesHtml}
                </div>
            `;
            container.innerHTML += cardHtml;
        }
    } catch (err) {
        console.error("Public services load failed", err);
    }
}

// Delete Service Logic
async function deleteService(id) {
    if (!confirm("¿Estás seguro de que deseas eliminar este servicio?")) return;
    try {
        const userData = JSON.parse(localStorage.getItem('user'));
        const res = await fetch(`${API_URL}/services/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + userData.accessToken }
        });
        if (res.ok) {
            alert('Servicio eliminado exitosamente');
            loadAdminServices(); // reload admin list
            loadPublicServices(); // sync public page
            loadServices(); // sync booking page
        } else {
            alert('Error al eliminar servicio');
        }
    } catch (err) {
        console.error(err);
        alert('Error de conexión');
    }
}

// Edit Service Logic
function openEditModal(id, category, name, desc, price, image) {
    document.getElementById('edit-service-id').value = id;
    document.getElementById('edit-service-category').value = category;
    document.getElementById('edit-service-name').value = name;
    document.getElementById('edit-service-desc').value = desc;
    document.getElementById('edit-service-price').value = price;
    document.getElementById('edit-service-image').value = image;
    document.getElementById('edit-service-container').style.display = 'block';
}

async function submitEditService(e) {
    e.preventDefault();
    const id = document.getElementById('edit-service-id').value;
    const userData = JSON.parse(localStorage.getItem('user'));
    const payload = {
        category: document.getElementById('edit-service-category').value,
        name: document.getElementById('edit-service-name').value,
        description: document.getElementById('edit-service-desc').value,
        price: document.getElementById('edit-service-price').value,
        imageUrl: document.getElementById('edit-service-image').value
    };

    try {
        const res = await fetch(`${API_URL}/services/${id}`, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + userData.accessToken
            },
            body: JSON.stringify(payload)
        });
        
        if (res.ok) {
            alert('Servicio actualizado exitosamente');
            document.getElementById('edit-service-container').style.display = 'none';
            loadAdminServices(); // reload admin list
            loadPublicServices(); // sync public page
            loadServices(); // sync booking page
        } else {
            alert('Error al actualizar servicio');
        }
    } catch (err) {
        console.error(err);
        alert('Error de conexión');
    }
}

// Create Service (Admin)
document.getElementById('create-service-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('service-name').value;
    const price = document.getElementById('service-price').value;
    const userData = JSON.parse(localStorage.getItem('user'));

    try {
        const res = await fetch(`${API_URL}/services`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${userData.accessToken}`
            },
            body: JSON.stringify({ name, description: '', price, durationMinutes: 60 })
        });
        
        if (res.ok) {
            showMessage('admin-message', 'Servicio creado exitosamente', false);
            document.getElementById('create-service-form').reset();
        } else {
            showMessage('admin-message', 'Error al crear servicio', true);
        }
    } catch (err) {
        showMessage('admin-message', 'Error de conexión', true);
    }
});

// --- Booking Logic ---

let currentBookingServiceId = null;

async function openBookingModal(serviceId, serviceName) {
    currentBookingServiceId = serviceId;
    document.getElementById('booking-service-name').textContent = `Servicio: ${serviceName}`;
    document.getElementById('booking-modal').classList.add('active');

    // UX: limitar el datetime picker a fechas futuras (a partir de "ahora + 1 min").
    // Es defensa en profundidad: el backend igual valida que no sea pasado.
    const dt = document.getElementById('booking-datetime');
    if (dt) {
        const now = new Date(Date.now() + 60000);
        const pad = (n) => String(n).padStart(2, '0');
        dt.min = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`;
    }

    // Fetch therapists
    const userData = JSON.parse(localStorage.getItem('user'));
    const token = userData.accessToken;
    try {
        const res = await fetch(`${API_URL}/users/therapists`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (res.ok) {
            const therapists = await res.json();
            const select = document.getElementById('booking-therapist');
            select.innerHTML = '<option value="">Selecciona un terapeuta</option>';
            if (therapists.length === 0) {
                select.innerHTML = '<option value="">No hay terapeutas disponibles</option>';
            }
            therapists.forEach(t => {
                select.innerHTML += `<option value="${t.id}">${t.name} (@${t.username})</option>`;
            });
        } else {
            const select = document.getElementById('booking-therapist');
            select.innerHTML = '<option value="">Error cargando terapeutas</option>';
            console.error('Error fetching therapists:', res.status);
        }
    } catch (err) {
        const select = document.getElementById('booking-therapist');
        select.innerHTML = '<option value="">Error de conexión</option>';
        console.error("Error loading therapists", err);
    }
}

function closeBookingModal() {
    document.getElementById('booking-modal').classList.remove('active');
    document.getElementById('booking-form').reset();
    document.getElementById('booking-message').textContent = '';
}

document.getElementById('booking-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const therapistId = document.getElementById('booking-therapist').value;
    const datetime = document.getElementById('booking-datetime').value;
    const userData = JSON.parse(localStorage.getItem('user'));
    const token = userData.accessToken;

    if (!therapistId) {
        showMessage('booking-message', 'Por favor selecciona un terapeuta.', true);
        return;
    }
    if (!datetime) {
        showMessage('booking-message', 'Por favor selecciona una fecha y hora.', true);
        return;
    }

    // Evitar doble-click: deshabilitar el botón mientras dura la request.
    // Sin esto, el cliente puede crear 2 citas idénticas si hace doble click rápido.
    const submitBtn = e.target.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    const originalLabel = submitBtn.textContent;
    submitBtn.textContent = 'Agendando...';

    try {
        const res = await fetch(`${API_URL}/appointments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                serviceId: currentBookingServiceId,
                therapistId: therapistId,
                appointmentTime: datetime
            })
        });

        const data = await res.json().catch(() => ({}));

        if (res.ok) {
            showMessage('booking-message', '¡Cita agendada exitosamente!', false);
            setTimeout(closeBookingModal, 2000);

            // Refrescar paneles si están abiertos
            if (userData.roles.includes('ROLE_ADMIN')) loadAllAppointments();
            if (userData.roles.includes('ROLE_THERAPIST')) loadTherapistAppointments();
            if (userData.roles.includes('ROLE_CLIENT')) loadClientAppointments();
        } else {
            // El backend devuelve el mensaje específico:
            //  - conflicto de overlap  -> "Esa franja horaria ya está reservada... Disponibilidad más cercana: ..."
            //  - cita en el pasado     -> "No se puede agendar una cita en el pasado..."
            //  - formato inválido      -> "Formato de fecha inválido."
            const msg = data.message || 'No fue posible agendar la cita.';
            showMessage('booking-message', msg, true);
        }
    } catch (err) {
        showMessage('booking-message', 'Error de conexión', true);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = originalLabel;
    }
});

// ==========================================
// Gestion de Usuarios y Roles (ADMIN)
// ==========================================
const AVAILABLE_ROLES = [
    { key: 'ROLE_CLIENT',    label: 'Cliente' },
    { key: 'ROLE_THERAPIST', label: 'Terapeuta' },
    { key: 'ROLE_ADMIN',     label: 'Admin' }
];

async function loadUsersForRoleManagement() {
    const list = document.getElementById('users-list');
    if (!list) return;
    list.innerHTML = '<p>Cargando usuarios...</p>';

    const userData = JSON.parse(localStorage.getItem('user'));
    try {
        const res = await fetch(`${API_URL}/users`, {
            headers: { 'Authorization': 'Bearer ' + userData.accessToken }
        });
        if (!res.ok) {
            list.innerHTML = '<p style="color:#e74c3c;">Error cargando usuarios.</p>';
            return;
        }
        const users = await res.json();
        list.innerHTML = '';
        users.forEach(u => {
            const currentRoles = u.roles || [];
            const checkboxes = AVAILABLE_ROLES.map(r => {
                const checked = currentRoles.includes(r.key) ? 'checked' : '';
                return `
                    <label style="display:inline-flex; align-items:center; gap:6px; margin-right:15px; font-size:0.9em; cursor:pointer;">
                        <input type="checkbox" value="${r.key}" ${checked}> ${r.label}
                    </label>
                `;
            }).join('');

            list.innerHTML += `
                <div class="user-row" data-user-id="${u.id}" style="background: rgba(255,255,255,0.05); padding: 15px; border-radius: 8px; display:flex; flex-wrap:wrap; justify-content:space-between; align-items:center; gap:15px;">
                    <div style="flex: 1; min-width:200px;">
                        <h4 style="margin:0 0 4px 0; font-size:1em;">${u.name} <span style="font-size:0.85em; color: var(--text-light); font-weight:normal;">(@${u.username})</span></h4>
                        <p style="margin:0; font-size:0.8em; color: var(--text-light);">${u.email}</p>
                    </div>
                    <div style="flex: 2; min-width:280px;">
                        ${checkboxes}
                    </div>
                    <button class="btn primary" style="padding: 6px 14px; font-size:0.85em;"
                            onclick="saveUserRoles(${u.id}, this)">Guardar</button>
                </div>
            `;
        });
    } catch (err) {
        list.innerHTML = '<p style="color:#e74c3c;">Error de conexion al cargar usuarios.</p>';
    }
}

async function saveUserRoles(userId, buttonEl) {
    const row = buttonEl.closest('.user-row');
    const checkedRoles = Array.from(row.querySelectorAll('input[type="checkbox"]:checked'))
        .map(cb => cb.value);

    if (checkedRoles.length === 0) {
        showUsersMgmtMessage('Debes seleccionar al menos un rol.', true);
        return;
    }

    const userData = JSON.parse(localStorage.getItem('user'));
    buttonEl.disabled = true;
    const original = buttonEl.textContent;
    buttonEl.textContent = '...';

    try {
        const res = await fetch(`${API_URL}/users/${userId}/roles`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + userData.accessToken
            },
            body: JSON.stringify({ roles: checkedRoles })
        });
        const data = await res.json().catch(() => ({}));
        if (res.ok) {
            showUsersMgmtMessage(data.message || 'Roles actualizados.', false);
            // refrescar para mostrar el estado final desde BD (idempotente)
            loadUsersForRoleManagement();
        } else {
            showUsersMgmtMessage(data.message || 'Error al actualizar roles.', true);
        }
    } catch (err) {
        showUsersMgmtMessage('Error de conexion.', true);
    } finally {
        buttonEl.disabled = false;
        buttonEl.textContent = original;
    }
}

function showUsersMgmtMessage(text, isError) {
    const el = document.getElementById('users-management-message');
    if (!el) return;
    el.textContent = text;
    el.className = 'message ' + (isError ? 'error' : 'success');
    setTimeout(() => { el.textContent = ''; el.className = 'message'; }, 4000);
}

// Cancelar cita (cliente o admin) — llama PUT /api/appointments/{id}/cancel
async function cancelMyAppointment(appointmentId) {
    if (!confirm('¿Estás seguro de que querés cancelar esta cita?')) return;
    const userData = JSON.parse(localStorage.getItem('user'));
    try {
        const res = await fetch(`${API_URL}/appointments/${appointmentId}/cancel`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${userData.accessToken}` }
        });
        const data = await res.json().catch(() => ({}));
        if (res.ok) {
            alert(data.message || 'Cita cancelada.');
            // Refrescar el panel actual
            if (userData.roles.includes('ROLE_CLIENT')) loadClientAppointments();
            if (userData.roles.includes('ROLE_ADMIN')) loadAllAppointments();
        } else {
            alert(data.message || 'No fue posible cancelar la cita.');
        }
    } catch (err) {
        alert('Error de conexión al cancelar la cita.');
    }
}

// Init
checkAuth();

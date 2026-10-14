import { useEffect, useState } from "react";
import "./App.css";

const API = "http://localhost:8080/api/ducks";

const emptyForm = {
  color: "RED",
  size: "XLARGE",
  price: "",
  quantity: "",
};

async function apiRequest(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  });

  if (response.status === 204) {
    if (!response.ok) throw new Error("Request failed.");
    return null;
  }

  const text = await response.text();
  let data = null;

  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = null;
  }

  if (!response.ok) {
    throw new Error(
      data?.message ||
        data?.detail ||
        data?.error ||
        text ||
        `Request failed (${response.status})`
    );
  }

  return data;
}

export default function App() {
  const [ducks, setDucks] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [search, setSearch] = useState("");

  async function loadDucks() {
    setLoading(true);
    setError("");

    try {
      const data = await apiRequest(API);
      setDucks(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(`Could not load ducks: ${err.message}`);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadDucks();
  }, []);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  }

  function resetForm() {
    setForm(emptyForm);
    setEditingId(null);
  }

  function startEdit(duck) {
    setEditingId(duck.id);
    setForm({
      color: duck.color,
      size: duck.size,
      price: String(duck.price),
      quantity: String(duck.quantity),
    });
    setMessage("");
    setError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");

    const payload = editingId
      ? {
          price: Number(form.price),
          quantity: Number(form.quantity),
        }
      : {
          color: form.color,
          size: form.size,
          price: Number(form.price),
          quantity: Number(form.quantity),
        };

    try {
      await apiRequest(
        editingId ? `${API}/${editingId}` : API,
        {
          method: editingId ? "PUT" : "POST",
          body: JSON.stringify(payload),
        }
      );

      setMessage(
        editingId
          ? "Duck updated successfully."
          : "Duck saved successfully. Matching ducks should merge according to the warehouse rules."
      );
      resetForm();
      await loadDucks();
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(duck) {
    const confirmed = window.confirm(
      `Delete the ${duck.color} ${duck.size} duck (ID ${duck.id})?`
    );

    if (!confirmed) return;

    setError("");
    setMessage("");

    try {
      await apiRequest(`${API}/${duck.id}`, { method: "DELETE" });
      setMessage("Duck deleted successfully.");
      await loadDucks();
    } catch (err) {
      setError(err.message);
    }
  }

  const visibleDucks = ducks
    .filter((duck) => {
      const value = `${duck.id} ${duck.color} ${duck.size}`.toLowerCase();
      return value.includes(search.toLowerCase().trim());
    })
    .sort((a, b) => Number(a.quantity) - Number(b.quantity));

  const totalUnits = ducks.reduce(
    (sum, duck) => sum + Number(duck.quantity || 0),
    0
  );

  return (
    <main className="app-shell">
      <header className="topbar">
        <div className="brand">
          <div className="brand-icon" aria-hidden="true">D</div>
          <div>
            <p className="eyebrow">WAREHOUSE MANAGEMENT</p>
            <h1>Duck Store</h1>
          </div>
        </div>
        <span className="status-pill">
          <span className="status-dot" />
          Inventory dashboard
        </span>
      </header>

      <section className="page-heading">
        <div>
          <p className="eyebrow">OVERVIEW</p>
          <h2>Duck Warehouse</h2>
          <p className="subtitle">
            Manage inventory, update stock, and keep your catalogue organized.
          </p>
        </div>
        <button
          className="button button-secondary"
          type="button"
          onClick={loadDucks}
          disabled={loading}
        >
          ↻ Refresh
        </button>
      </section>

      <section className="stats-grid">
        <article className="stat-card">
          <span className="stat-label">Duck records</span>
          <strong>{ducks.length}</strong>
          <span className="stat-note">Active inventory entries</span>
        </article>
        <article className="stat-card">
          <span className="stat-label">Total units in stock</span>
          <strong>{totalUnits.toLocaleString()}</strong>
          <span className="stat-note">Across all active records</span>
        </article>
        <article className="stat-card">
          <span className="stat-label">Matching records</span>
          <strong>{visibleDucks.length}</strong>
          <span className="stat-note">Based on current search</span>
        </article>
      </section>

      <section className="panel form-panel">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">{editingId ? "EDIT RECORD" : "NEW INVENTORY"}</p>
            <h3>{editingId ? `Edit duck #${editingId}` : "Add duck to warehouse"}</h3>
          </div>
          {editingId && (
            <button
              className="button button-secondary"
              type="button"
              onClick={resetForm}
            >
              Cancel edit
            </button>
          )}
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <label>
              Color
              <select
                name="color"
                value={form.color}
                onChange={handleChange}
                disabled={Boolean(editingId)}
                required
              >
                <option value="RED">Red</option>
                <option value="GREEN">Green</option>
                <option value="YELLOW">Yellow</option>
                <option value="BLACK">Black</option>
              </select>
            </label>

            <label>
              Size
              <select
                name="size"
                value={form.size}
                onChange={handleChange}
                disabled={Boolean(editingId)}
                required
              >
                <option value="XLARGE">XLarge</option>
                <option value="LARGE">Large</option>
                <option value="MEDIUM">Medium</option>
                <option value="SMALL">Small</option>
                <option value="XSMALL">XSmall</option>
              </select>
            </label>

            <label>
              Unit price (USD)
              <input
                name="price"
                type="number"
                min="0.01"
                step="0.01"
                placeholder="e.g. 25.00"
                value={form.price}
                onChange={handleChange}
                required
              />
            </label>

            <label>
              Quantity
              <input
                name="quantity"
                type="number"
                min={editingId ? "0" : "1"}
                step="1"
                placeholder="Enter units"
                value={form.quantity}
                onChange={handleChange}
                required
              />
            </label>
          </div>

          {editingId && (
            <p className="form-hint">
              Color and size cannot be changed when editing a record.
            </p>
          )}

          <div className="form-actions">
            <button className="button button-primary" type="submit" disabled={saving}>
              {saving
                ? "Saving..."
                : editingId
                  ? "Save changes"
                  : "Add duck"}
            </button>
            {!editingId && (
              <span className="form-hint">
                Matching color, size, and price should merge quantities.
              </span>
            )}
          </div>
        </form>
      </section>

      {error && (
        <div className="alert alert-error" role="alert">
          <strong>Something went wrong:</strong> {error}
        </div>
      )}

      {message && (
        <div className="alert alert-success" role="status">
          {message}
        </div>
      )}

      <section className="panel inventory-panel">
        <div className="panel-heading inventory-heading">
          <div>
            <p className="eyebrow">STOCK MANAGEMENT</p>
            <h3>Inventory records</h3>
            <p className="subtitle small-subtitle">
              Sorted by quantity, lowest first.
            </p>
          </div>
          <label className="search-box">
            <span className="sr-only">Search inventory</span>
            <span aria-hidden="true">⌕</span>
            <input
              type="search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search color, size or ID"
            />
          </label>
        </div>

        {loading ? (
          <div className="empty-state">
            <div className="loader" />
            <p>Loading warehouse inventory...</p>
          </div>
        ) : visibleDucks.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">D</div>
            <h4>{search ? "No matching ducks" : "Your warehouse is empty"}</h4>
            <p>
              {search
                ? "Try another search term."
                : "Use the form above to add your first inventory record."}
            </p>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Color</th>
                  <th>Size</th>
                  <th>Unit price</th>
                  <th>Quantity</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {visibleDucks.map((duck) => (
                  <tr key={duck.id}>
                    <td className="id-cell">#{duck.id}</td>
                    <td>
                      <span className="color-cell">
                        <span className={`color-dot color-${String(duck.color).toLowerCase()}`} />
                        {String(duck.color).charAt(0) +
                          String(duck.color).slice(1).toLowerCase()}
                      </span>
                    </td>
                    <td>{duck.size}</td>
                    <td className="price-cell">
                      ${Number(duck.price).toFixed(2)}
                    </td>
                    <td>
                      <span className="quantity-badge">
                        {Number(duck.quantity).toLocaleString()}
                      </span>
                    </td>
                    <td>
                      <div className="row-actions">
                        <button
                          className="button button-edit"
                          onClick={() => startEdit(duck)}
                          type="button"
                        >
                          Edit
                        </button>
                        <button
                          className="button button-delete"
                          onClick={() => handleDelete(duck)}
                          type="button"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <footer className="table-footer">
          <span>{visibleDucks.length} record(s) displayed</span>
          <span>Prices displayed in USD</span>
        </footer>
      </section>

      <footer className="app-footer">
        Digital Harbor · Duck Store Coding Exercise
      </footer>
    </main>
  );
}
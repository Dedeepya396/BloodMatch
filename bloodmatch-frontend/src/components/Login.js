import { useState } from "react";
import { useNavigate } from "react-router-dom";

function Login() {
  const [form, setForm] = useState({ role: 'DONOR', email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async () => {
    setLoading(true); setError('');
    try {
      const res = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });
      if (!res.ok) {
        const txt = await res.text(); setError(txt || 'Login failed'); return;
      }
      const data = await res.json();
      // store basic auth info (no JWT)
      localStorage.setItem('bm_auth', JSON.stringify({ id: data.id, role: data.role, name: data.name, email: data.email }));
      if (data.role === 'DONOR') navigate('/profile');
      else if (data.role === 'HOSPITAL') navigate('/request');
      else if (data.role === 'BLOOD_BANK') navigate('/blood-bank-dashboard');
      else navigate('/');
    } catch (err) {
      console.log(err);
      setError('Network error');
    }
    finally { setLoading(false); }
  };

  return (
    <div className="panel">
      <div className="panel-header">
        <div className="panel-icon blue">🔐</div>
        <div>
          <div className="panel-title">Sign In</div>
          <div className="panel-desc">Enter your account credentials</div>
        </div>
      </div>

      <div className="form-grid full">
        <div className="field">
          <div className="role-selector" style={{ display: 'flex', gap: 10, marginBottom: 10 }}>
            {['DONOR', 'HOSPITAL', 'BLOOD_BANK'].map(r => (
              <button
                key={r}
                type="button"
                onClick={() => setForm(f => ({ ...f, role: r }))}
                style={{
                  flex: 1, padding: "10px", borderRadius: 10, cursor: "pointer", fontWeight: 700, fontSize: '13px',
                  background: form.role === r ? "rgba(220,38,38,0.1)" : "#f8fafc",
                  color: form.role === r ? "#dc2626" : "#475569",
                  border: form.role === r ? "1px solid #dc2626" : "1px solid rgba(0,0,0,0.06)",
                  transition: "all 0.2s"
                }}
              >
                {r.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>
        <div className="field">
          <label>Email</label>
          <input name="email" value={form.email} onChange={handleChange} />
        </div>
        <div className="field">
          <label>Password</label>
          <input name="password" type="password" value={form.password} onChange={handleChange} />
        </div>
      </div>

      {error && <div style={{ color: '#fb7185', marginTop: 8 }}>{error}</div>}

      <button className="btn btn-blue" onClick={submit} disabled={loading}>
        {loading ? 'Signing in…' : `Sign In as ${form.role.replace('_', ' ')}`}
      </button>
    </div>
  );
}

export default Login;

import { useState } from "react";
import MapPicker from "./MapPicker";
import { useNavigate } from "react-router-dom";

function Signup() {
    const [role, setRole] = useState('DONOR');
    const [form, setForm] = useState({
        name: '', email: '', password: '',
        // donor
        bloodGroup: '', latitude: '', longitude: '', lastDonationDate: '',
        // hospital & blood bank
        address: '', contactNumber: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    const onMapChange = ({ lat, lng, display_name }) => {
        setForm({ ...form, latitude: String(lat), longitude: String(lng), address: display_name || form.address });
    };

    const submit = async () => {
        setLoading(true); setError('');
        try {
            const payload = { role, name: form.name, email: form.email, password: form.password };
            if (role === 'DONOR') {
                payload.bloodGroup = form.bloodGroup;
                payload.latitude = parseFloat(form.latitude || 0);
                payload.longitude = parseFloat(form.longitude || 0);
                payload.available = true;
                if (form.lastDonationDate) payload.lastDonationDate = form.lastDonationDate;
            } else if (role === 'HOSPITAL') {
                payload.address = form.address;
                payload.contactNumber = form.contactNumber;
                payload.latitude = parseFloat(form.latitude || 0);
                payload.longitude = parseFloat(form.longitude || 0);
            } else if (role === 'BLOOD_BANK') {
                payload.address = form.address;
                payload.contactNumber = form.contactNumber;
                payload.latitude = parseFloat(form.latitude || 0);
                payload.longitude = parseFloat(form.longitude || 0);
            }
            const res = await fetch('http://localhost:8080/api/auth/signup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!res.ok) { const txt = await res.text(); setError(txt || 'Signup failed'); return; }
            const data = await res.json();
            localStorage.setItem('bm_auth', JSON.stringify({ id: data.id, role: data.role, name: data.name, email: data.email }));
            if (data.role === 'DONOR') navigate('/profile');
            else if (data.role === 'HOSPITAL') navigate('/request');
            else if (data.role === 'BLOOD_BANK') navigate('/blood-bank-dashboard');
            else navigate('/');
        } catch (err) { setError('Network error'); }
        finally { setLoading(false); }
    };

    return (
        <div className="panel">
            <div className="panel-header">
                <div className="panel-icon blue">✍️</div>
                <div>
                    <div className="panel-title">Sign Up</div>
                    <div className="panel-desc">Create a new account</div>
                </div>
            </div>

            <div className="form-grid" style={{ marginBottom: 20 }}>
                <button className={`btn ${role === 'DONOR' ? 'btn-red' : 'btn-outline'}`} onClick={() => setRole('DONOR')} type="button" style={{ width: '100%', marginTop: 0, background: role === 'DONOR' ? undefined : '#f8fafc', color: role === 'DONOR' ? 'white' : '#475569', borderColor: role === 'DONOR' ? '#dc2626' : 'rgba(0,0,0,0.1)' }}>Donor</button>
                <button className={`btn ${role === 'HOSPITAL' ? 'btn-red' : 'btn-outline'}`} onClick={() => setRole('HOSPITAL')} type="button" style={{ width: '100%', marginTop: 0, background: role === 'HOSPITAL' ? undefined : '#f8fafc', color: role === 'HOSPITAL' ? 'white' : '#475569', borderColor: role === 'HOSPITAL' ? '#dc2626' : 'rgba(0,0,0,0.1)' }}>Hospital</button>
                <div className="field span-2">
                    <button className={`btn ${role === 'BLOOD_BANK' ? 'btn-red' : 'btn-outline'}`} onClick={() => setRole('BLOOD_BANK')} type="button" style={{ width: '100%', marginTop: 0, background: role === 'BLOOD_BANK' ? undefined : '#f8fafc', color: role === 'BLOOD_BANK' ? 'white' : '#475569', borderColor: role === 'BLOOD_BANK' ? '#dc2626' : 'rgba(0,0,0,0.1)' }}>🏦 Blood Bank</button>
                </div>
            </div>

            <div className="form-grid">
                <div className="field span-2">
                    <label>Full name</label>
                    <input name="name" value={form.name} onChange={handleChange} placeholder="Enter your full name" />
                </div>
                <div className="field span-2">
                    <label>Email Address</label>
                    <input name="email" value={form.email} onChange={handleChange} placeholder="name@example.com" />
                </div>
                <div className="field">
                    <label>Password</label>
                    <input name="password" type="password" value={form.password} onChange={handleChange} placeholder="••••••••" />
                </div>

                {role === 'DONOR' && (
                    <>
                        <div className="field">
                            <label>Blood Group</label>
                            <select name="bloodGroup" value={form.bloodGroup} onChange={handleChange}>
                                <option value="">Select Group</option>
                                {['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'].map(bg => <option key={bg} value={bg}>{bg}</option>)}
                            </select>
                        </div>
                        <div className="field span-2">
                            <label>Last Donation Date</label>
                            <input name="lastDonationDate" type="date" value={form.lastDonationDate} onChange={handleChange} />
                        </div>
                    </>
                )}

                {role === 'HOSPITAL' && (
                    <>
                        <div className="field">
                            <label>Contact Number</label>
                            <input name="contactNumber" value={form.contactNumber} onChange={handleChange} placeholder="+91 xxxxx xxxxx" />
                        </div>
                        <div className="field span-2">
                            <label>Hospital Address</label>
                            <input name="address" value={form.address} onChange={handleChange} placeholder="Street, City, Pincode" />
                        </div>
                    </>
                )}

                {role === 'BLOOD_BANK' && (
                    <>
                        <div className="field">
                            <label>Contact Number</label>
                            <input name="contactNumber" value={form.contactNumber} onChange={handleChange} placeholder="+91 xxxxx xxxxx" />
                        </div>
                        <div className="field span-2">
                            <label>Blood Bank Address</label>
                            <input name="address" value={form.address} onChange={handleChange} placeholder="Street, City, Pincode" />
                        </div>
                    </>
                )}

                {(role === 'DONOR' || role === 'HOSPITAL' || role === 'BLOOD_BANK') && (
                    <div className="field span-2" style={{ marginTop: 8 }}>
                        <label>Select Location on Map</label>
                        <MapPicker
                            position={form.latitude && form.longitude ? [parseFloat(form.latitude), parseFloat(form.longitude)] : null}
                            onChange={onMapChange}
                        />
                    </div>
                )}
            </div>

            {error && <div style={{ color: '#dc2626', marginTop: 16, fontWeight: 600, fontSize: '13px' }}>{error}</div>}

            <button className="btn btn-red" onClick={submit} disabled={loading} style={{ marginTop: 32 }}>
                {loading ? 'Creating Account…' : 'Create Account'}
            </button>
        </div>
    );
}

export default Signup;

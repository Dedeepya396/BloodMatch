import { useState, useRef, useEffect } from "react";
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

function Recenter({ center }) {
  const map = useMap();
  useEffect(() => {
    if (center) map.setView(center, map.getZoom());
  }, [center, map]);
  return null;
}

export default function MapPicker({ position, onChange, placeholder = "Search place or click on map" }) {
  const [center, setCenter] = useState(position || [19.07, 72.87]);
  const [markerPos, setMarkerPos] = useState(position || null);
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const debounceRef = useRef(null);

  useEffect(() => {
    if (position) {
      setCenter(position);
      setMarkerPos(position);
    }
  }, [position]);

  const doSearch = (q) => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    if (!q || q.length < 2) { setSuggestions([]); return; }
    debounceRef.current = setTimeout(async () => {
      try {
        const res = await fetch(
          `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(q)}&addressdetails=1&limit=6`,
          { headers: { Accept: "application/json" } }
        );
        setSuggestions(await res.json() || []);
      } catch (err) { console.error(err); }
    }, 300);
  };

  const selectSuggestion = (it) => {
    const lat = parseFloat(it.lat);
    const lng = parseFloat(it.lon);
    setMarkerPos([lat, lng]);
    setCenter([lat, lng]);
    setQuery("");
    setSuggestions([]);
    onChange?.({ lat, lng, display_name: it.display_name });
  };

  const useCurrent = () => {
    if (!navigator.geolocation) return alert("Geolocation not supported");
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => {
        const { latitude: lat, longitude: lng } = coords;
        setMarkerPos([lat, lng]);
        setCenter([lat, lng]);
        onChange?.({ lat, lng });
      },
      () => alert("Unable to get location")
    );
  };

  function MapEvents() {
    useMapEvents({
      click(e) {
        const { lat, lng } = e.latlng;
        setMarkerPos([lat, lng]);
        setCenter([lat, lng]);
        onChange?.({ lat, lng });
      },
    });
    return null;
  }

  return (
    <div className="map-picker">
      {/* Search row */}
      <div className="map-search-row">
        <div className="map-search-wrap">
          <svg className="map-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
            <circle cx="11" cy="11" r="8" /><path d="m21 21-4.35-4.35" />
          </svg>
          <input
            className="map-search-input"
            value={query}
            placeholder={placeholder}
            onChange={(e) => { setQuery(e.target.value); doSearch(e.target.value); }}
          />
        </div>
        <button type="button" className="map-location-btn" onClick={useCurrent}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} width={14} height={14}>
            <circle cx="12" cy="12" r="3" />
            <path d="M12 2v3m0 14v3M2 12h3m14 0h3" />
          </svg>
          My location
        </button>
      </div>

      {/* Suggestions dropdown */}
      {suggestions.length > 0 && (
        <div className="map-suggestions">
          {suggestions.map((it) => (
            <div key={it.place_id} className="map-suggestion-item" onClick={() => selectSuggestion(it)}>
              <div className="map-suggestion-title">{it.display_name.split(",")[0]}</div>
              <div className="map-suggestion-sub">{it.display_name}</div>
            </div>
          ))}
        </div>
      )}

      {/* Map */}
      <div className="map-container-wrap">
        <MapContainer center={center} zoom={13} style={{ height: "100%", width: "100%" }}>
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution="&copy; OpenStreetMap"
          />
          <Recenter center={center} />
          <MapEvents />
          {markerPos && (
            <Marker
              position={markerPos}
              draggable
              eventHandlers={{
                dragend: (e) => {
                  const { lat, lng } = e.target.getLatLng();
                  setMarkerPos([lat, lng]);
                  onChange?.({ lat, lng });
                },
              }}
            />
          )}
        </MapContainer>
      </div>
    </div>
  );
}
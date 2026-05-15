import { useState, useEffect, useRef } from 'react';
import { Stethoscope, Camera, Save, User } from 'lucide-react';
import api from '../../services/api';
import toast from 'react-hot-toast';

/**
 * ProviderEditProfile — Provider-only page to edit professional profile.
 * Separate from ProfileSettings because it manages provider-specific fields
 * like specialization, qualifications, bio, clinic details, and fees.
 */
export default function ProviderEditProfile() {
  const [profile, setProfile] = useState({
    specialization: '', qualifications: '', bio: '', experienceYears: '',
    clinicName: '', clinicAddress: '', consultationFee: '', profilePicture: '',
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef(null);

  useEffect(() => { loadProfile(); }, []);

  const loadProfile = async () => {
    try {
      const data = await api.get('/providers/me/profile');
      setProfile({
        specialization: data.specialization || '',
        qualifications: data.qualifications || '',
        bio: data.bio || '',
        experienceYears: data.experienceYears || '',
        clinicName: data.clinicName || '',
        clinicAddress: data.clinicAddress || '',
        consultationFee: data.consultationFee || '',
        profilePicture: data.profilePicture || '',
      });
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await api.put('/providers/me/profile', {
        specialization: profile.specialization || null,
        qualifications: profile.qualifications || null,
        bio: profile.bio || null,
        experienceYears: profile.experienceYears ? parseInt(profile.experienceYears) : null,
        clinicName: profile.clinicName || null,
        clinicAddress: profile.clinicAddress || null,
        consultationFee: profile.consultationFee ? parseFloat(profile.consultationFee) : null,
      });
      toast.success('Professional profile updated!');
    } catch (err) {
      toast.error(err.message);
    } finally {
      setSaving(false);
    }
  };

  const handlePictureUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) { toast.error('Only images allowed'); return; }
    if (file.size > 5 * 1024 * 1024) { toast.error('Max 5MB'); return; }

    setUploading(true);
    try {
      const res = await api.upload('/providers/me/picture', file);
      setProfile({ ...profile, profilePicture: res.profilePicture });
      toast.success('Photo updated!');
    } catch (err) {
      toast.error(err.message);
    } finally {
      setUploading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
        <Stethoscope className="w-6 h-6" /> Edit Professional Profile
      </h1>

      {/* Profile Picture */}
      <div className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
        <div className="flex items-center gap-6">
          <div className="relative">
            <div className="w-24 h-24 rounded-full bg-gray-200 overflow-hidden">
              {profile.profilePicture ? (
                <img src={profile.profilePicture} alt="Doctor" className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-gray-400">
                  <User className="w-12 h-12" />
                </div>
              )}
            </div>
            <button onClick={() => fileInputRef.current?.click()} disabled={uploading}
              className="absolute -bottom-1 -right-1 bg-blue-600 text-white p-2 rounded-full hover:bg-blue-700 shadow-md">
              {uploading ? (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              ) : (
                <Camera className="w-4 h-4" />
              )}
            </button>
            <input ref={fileInputRef} type="file" accept="image/*" className="hidden" onChange={handlePictureUpload} />
          </div>
          <div>
            <p className="font-medium text-gray-900">Doctor Profile Photo</p>
            <p className="text-sm text-gray-500">JPEG or PNG, max 5MB. Patients see this on your profile.</p>
          </div>
        </div>
      </div>

      {/* Professional Details Form */}
      <form onSubmit={handleSave} className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm space-y-5">
        <h2 className="text-lg font-semibold text-gray-900 mb-1">Professional Details</h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Specialization</label>
            <input type="text" value={profile.specialization}
              onChange={(e) => setProfile({ ...profile, specialization: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
              placeholder="e.g. Cardiology" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Experience (Years)</label>
            <input type="number" min="0" value={profile.experienceYears}
              onChange={(e) => setProfile({ ...profile, experienceYears: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
              placeholder="e.g. 10" />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Qualifications</label>
          <input type="text" value={profile.qualifications}
            onChange={(e) => setProfile({ ...profile, qualifications: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
            placeholder="e.g. MBBS, MD, DM Cardiology" />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Bio</label>
          <textarea rows={3} value={profile.bio}
            onChange={(e) => setProfile({ ...profile, bio: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm resize-none"
            placeholder="Tell patients about yourself, your approach, and expertise..." />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Consultation Fee (₹)</label>
          <input type="number" min="0" step="50" value={profile.consultationFee}
            onChange={(e) => setProfile({ ...profile, consultationFee: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
            placeholder="e.g. 500" />
        </div>

        <h2 className="text-lg font-semibold text-gray-900 pt-2">Clinic Details</h2>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Clinic Name</label>
          <input type="text" value={profile.clinicName}
            onChange={(e) => setProfile({ ...profile, clinicName: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm"
            placeholder="e.g. Heart Care Clinic" />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Clinic Address</label>
          <textarea rows={2} value={profile.clinicAddress}
            onChange={(e) => setProfile({ ...profile, clinicAddress: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 text-sm resize-none"
            placeholder="Full clinic address" />
        </div>

        <button type="submit" disabled={saving}
          className="bg-blue-600 text-white px-6 py-2.5 rounded-lg hover:bg-blue-700 disabled:opacity-50 font-medium text-sm flex items-center gap-2 transition-colors">
          <Save className="w-4 h-4" />
          {saving ? 'Saving...' : 'Save Professional Profile'}
        </button>
      </form>
    </div>
  );
}

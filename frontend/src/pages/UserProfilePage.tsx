import { useEffect, useState } from "react";
import api from "../lib/api";
import { useOutletContext } from "react-router-dom";

export default function UserProfilePage() {
  const { refreshGlobalProfile } = useOutletContext<any>() || {};
  const [profile, setProfile] = useState<any>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [errors, setErrors] = useState<any>({});
  
  const [editForm, setEditForm] = useState({
    fullName: "",
    phoneNumber: "",
    email: "",
    username: ""
  });

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    setIsLoading(true);
    try {
      const response = await api.get("/api/v1/user/profile");
      if (response.data && (response.data.restApiResponseHttpCode === 200 || response.data.code === 200)) {
        const data = response.data.restApiResponseData || response.data.data;
        setProfile(data);
        setEditForm({
          fullName: data.fullName || "",
          phoneNumber: data.phoneNumber || "",
          email: data.email || "",
          username: data.username || ""
        });
      }
    } catch (error) {
      console.error("Failed to fetch user profile", error);
    } finally {
      setIsLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors: any = {};
    if (!editForm.fullName || editForm.fullName.trim().length < 3) {
      newErrors.fullName = "Full Name must be at least 3 characters long.";
    } else if (editForm.fullName.trim().length > 50) {
      newErrors.fullName = "Full Name cannot exceed 50 characters.";
    } else if (!/^[a-zA-Z\s]+$/.test(editForm.fullName)) {
      newErrors.fullName = "Full Name can only contain letters and spaces.";
    }

    if (!editForm.phoneNumber || editForm.phoneNumber.trim().length < 10) {
      newErrors.phoneNumber = "Phone Number must be at least 10 digits.";
    } else if (!/^\+?[0-9]{10,15}$/.test(editForm.phoneNumber)) {
      newErrors.phoneNumber = "Phone Number can only contain numbers and an optional leading '+'.";
    }

    if (!editForm.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(editForm.email)) {
      newErrors.email = "Please enter a valid email address.";
    }

    if (!editForm.username || editForm.username.trim().length < 3) {
      newErrors.username = "Username must be at least 3 characters long.";
    } else if (editForm.username.trim().length > 20) {
      newErrors.username = "Username cannot exceed 20 characters.";
    } else if (!/^[a-zA-Z0-9_]+$/.test(editForm.username)) {
      newErrors.username = "Username can only contain letters, numbers, and underscores.";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = async () => {
    if (!validateForm()) return;
    setIsSaving(true);
    try {
      await api.put("/api/v1/user/profile", editForm);
      setIsEditing(false);
      await fetchProfile(); 
      if (refreshGlobalProfile) refreshGlobalProfile();
    } catch (error: any) {
      console.error("Failed to update profile", error);
      alert(error.response?.data?.restApiResponseMessage || "Failed to update profile.");
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return <div className="p-4 text-on-surface-variant">Loading profile...</div>;
  }

  if (!profile) {
    return <div className="p-4 text-error">Failed to load profile.</div>;
  }

  return (
    <div className="space-y-gutter">

      <section className="bg-surface-container-lowest rounded-xl border border-outline-variant/30 shadow-sm overflow-hidden transition-all duration-300">
        {/* Header Banner */}
        <div className="bg-primary/10 relative px-stack-lg py-8 sm:py-12">
          <div className="absolute inset-0 bg-gradient-to-r from-primary/20 to-transparent"></div>
          
          <div className="relative z-10 flex flex-col sm:flex-row items-center sm:items-center gap-6">
            {/* Avatar */}
            <div className="w-24 h-24 sm:w-32 sm:h-32 rounded-full border-4 border-surface shadow-md bg-white flex items-center justify-center shrink-0">
              <span className="material-symbols-outlined text-[48px] sm:text-[64px] text-primary/70">person</span>
            </div>
            
            {/* User Info */}
            <div className="flex-grow text-center sm:text-left min-w-0">
              <div className="flex items-center justify-center sm:justify-start gap-2">
                <h2 className="font-display-sm text-on-surface font-bold truncate" title={profile.fullName}>{profile.fullName}</h2>
                <span className="material-symbols-outlined text-primary text-[24px] shrink-0">verified</span>
              </div>
              <p className="text-on-surface-variant font-body-lg mt-1 truncate">Member of JatiStore</p>
            </div>
            
            {/* Action Buttons */}
            <div className="shrink-0 mt-4 sm:mt-0">
              {!isEditing ? (
                <button 
                  onClick={() => setIsEditing(true)}
                  className="flex items-center gap-2 px-6 py-2.5 bg-primary text-on-primary font-label-md rounded-xl hover:bg-primary-container shadow-sm transition-all"
                >
                  <span className="material-symbols-outlined text-[20px]">edit</span>
                  Edit Profile
                </button>
              ) : (
                <div className="flex gap-2">
                  <button 
                    onClick={() => {
                      setIsEditing(false);
                      setErrors({});
                      setEditForm({ 
                        fullName: profile.fullName, 
                        phoneNumber: profile.phoneNumber,
                        email: profile.email,
                        username: profile.username
                      });
                    }}
                    className="flex items-center gap-2 px-4 py-2 border border-outline-variant text-on-surface font-label-md rounded-xl hover:bg-surface transition-all bg-surface"
                  >
                    Cancel
                  </button>
                  <button 
                    onClick={handleSave}
                    disabled={isSaving}
                    className="flex items-center gap-2 px-6 py-2 bg-primary text-on-primary font-label-md rounded-xl hover:bg-primary-container shadow-sm transition-all disabled:opacity-50"
                  >
                    {isSaving ? "Saving..." : "Save"}
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Content Body */}
        <div className="px-stack-lg pb-stack-lg">
          <div className="mt-8 grid grid-cols-1 md:grid-cols-2 gap-x-gutter gap-y-8">
            <div className="space-y-1.5">
              <span className="text-on-surface font-label-md font-bold uppercase tracking-widest">Full Name</span>
              {isEditing ? (
                <div>
                  <input 
                    type="text" 
                    maxLength={50}
                    value={editForm.fullName}
                    onChange={(e) => setEditForm({...editForm, fullName: e.target.value})}
                    className={`w-full bg-surface border ${errors.fullName ? 'border-error focus:border-error focus:ring-error' : 'border-outline-variant focus:border-primary focus:ring-primary'} rounded-lg px-3 py-2 text-body-md focus:ring-1`}
                  />
                  {errors.fullName && <p className="text-error text-body-sm mt-1">{errors.fullName}</p>}
                </div>
              ) : (
                <p className="font-body-md text-on-surface-variant">{profile.fullName}</p>
              )}
            </div>
            <div className="space-y-1.5">
              <span className="text-on-surface font-label-md font-bold uppercase tracking-widest">Phone Number</span>
              {isEditing ? (
                <div>
                  <input 
                    type="text" 
                    value={editForm.phoneNumber}
                    onChange={(e) => setEditForm({...editForm, phoneNumber: e.target.value})}
                    className={`w-full bg-surface border ${errors.phoneNumber ? 'border-error focus:border-error focus:ring-error' : 'border-outline-variant focus:border-primary focus:ring-primary'} rounded-lg px-3 py-2 text-body-md focus:ring-1`}
                  />
                  {errors.phoneNumber && <p className="text-error text-body-sm mt-1">{errors.phoneNumber}</p>}
                </div>
              ) : (
                <p className="font-body-md text-on-surface-variant">{profile.phoneNumber || "-"}</p>
              )}
            </div>
            <div className="space-y-1.5">
              <span className="text-on-surface font-label-md font-bold uppercase tracking-widest">Email Address</span>
              {isEditing ? (
                <div>
                  <input 
                    type="email" 
                    value={editForm.email}
                    onChange={(e) => setEditForm({...editForm, email: e.target.value})}
                    className={`w-full bg-surface border ${errors.email ? 'border-error focus:border-error focus:ring-error' : 'border-outline-variant focus:border-primary focus:ring-primary'} rounded-lg px-3 py-2 text-body-md focus:ring-1`}
                  />
                  {errors.email && <p className="text-error text-body-sm mt-1">{errors.email}</p>}
                </div>
              ) : (
                <p className="font-body-md text-on-surface-variant">{profile.email}</p>
              )}
            </div>
            <div className="space-y-1.5">
              <span className="text-on-surface font-label-md font-bold uppercase tracking-widest">Username</span>
              {isEditing ? (
                <div>
                  <input 
                    type="text" 
                    maxLength={20}
                    value={editForm.username}
                    onChange={(e) => setEditForm({...editForm, username: e.target.value})}
                    className={`w-full bg-surface border ${errors.username ? 'border-error focus:border-error focus:ring-error' : 'border-outline-variant focus:border-primary focus:ring-primary'} rounded-lg px-3 py-2 text-body-md focus:ring-1`}
                  />
                  {errors.username && <p className="text-error text-body-sm mt-1">{errors.username}</p>}
                </div>
              ) : (
                <p className="font-body-md text-on-surface-variant">{profile.username}</p>
              )}
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

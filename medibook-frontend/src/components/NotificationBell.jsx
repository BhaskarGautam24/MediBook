import { useState, useEffect, useRef } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Bell } from 'lucide-react';

export default function NotificationBell() {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  useEffect(() => {
    if (user?.id) {
      fetchNotifications();
    }
  }, [user]);

  // Close dropdown on outside click
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const fetchNotifications = async () => {
    try {
      const [notifsRes, countRes] = await Promise.all([
        api.get(`/notifications/${user.id}`),
        api.get(`/notifications/unread-count/${user.id}`)
      ]);
      setNotifications(notifsRes);
      setUnreadCount(countRes);
    } catch (err) {
      console.error("Failed to fetch notifications", err);
    }
  };

  const markAsRead = async (id) => {
    try {
      await api.put(`/notifications/read/${id}`);
      fetchNotifications();
    } catch (err) {
      console.error("Failed to mark as read", err);
    }
  };

  const markAllAsRead = async () => {
    try {
      await api.put(`/notifications/read-all/${user.id}`);
      fetchNotifications();
    } catch (err) {
      console.error("Failed to mark all as read", err);
    }
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        className="relative p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer"
        onClick={() => setIsOpen(!isOpen)}
        title="Notifications"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-[10px] font-bold rounded-full min-w-[18px] h-[18px] flex items-center justify-center px-1">
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute top-11 right-0 w-80 bg-white border border-gray-200 rounded-xl shadow-lg z-50 max-h-96 flex flex-col overflow-hidden">
          <div className="px-4 py-3 border-b border-gray-100 flex justify-between items-center bg-gray-50/80">
            <h4 className="text-sm font-semibold text-gray-900">Notifications</h4>
            {unreadCount > 0 && (
              <button
                className="text-xs text-blue-600 hover:text-blue-700 hover:underline cursor-pointer bg-transparent border-none"
                onClick={markAllAsRead}
              >
                Mark all read
              </button>
            )}
          </div>
          <div className="overflow-y-auto flex-1">
            {notifications.length === 0 ? (
              <p className="py-8 text-center text-sm text-gray-400">No notifications yet.</p>
            ) : (
              notifications.map((notif) => (
                <div
                  key={notif.notificationId}
                  className={`px-4 py-3 border-b border-gray-50 cursor-pointer transition-colors ${
                    notif.read
                      ? 'bg-white hover:bg-gray-50'
                      : 'bg-blue-50/60 hover:bg-blue-50'
                  }`}
                  onClick={() => !notif.read && markAsRead(notif.notificationId)}
                >
                  <strong className="block text-sm text-gray-900 mb-0.5">{notif.title}</strong>
                  <p className="text-xs text-gray-500 leading-relaxed mb-1">{notif.message}</p>
                  <small className="text-[11px] text-gray-400">{new Date(notif.sentAt).toLocaleString()}</small>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}

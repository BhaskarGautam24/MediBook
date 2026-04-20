/**
 * Generic placeholder page for routes not yet implemented.
 */
export default function PlaceholderPage({ title, description }) {
  return (
    <div className="flex flex-col items-center justify-center py-20">
      <div className="w-16 h-16 rounded-2xl bg-slate-800 border border-slate-700 flex items-center justify-center text-3xl mb-4">
        🚧
      </div>
      <h2 className="text-xl font-bold text-white mb-2">{title || 'Coming Soon'}</h2>
      <p className="text-sm text-gray-400 text-center max-w-md">
        {description || 'This page is under development and will be available soon.'}
      </p>
    </div>
  );
}

import React, { useEffect, useState } from 'react';
import { Search, FileSearch, RefreshCw } from 'lucide-react';
import { logApi } from '../api/client';
import { LogEntry } from '../types';
import { RawLogModal } from '../components/common/RawLogModal';

export default function GlobalLogs() {
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);

  const [keyword, setKeyword] = useState('');
  const [eventType, setEventType] = useState('');
  const [username, setUsername] = useState('');
  const [selectedLog, setSelectedLog] = useState<LogEntry | null>(null);

  const loadLogs = async () => {
    setLoading(true);
    try {
      const data = await logApi.searchGlobalLogs({
        keyword: keyword || undefined,
        eventType: eventType || undefined,
        username: username || undefined,
        page,
        size: 25,
      });
      setLogs(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (err) {
      console.error('Failed to load global logs', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadLogs();
  }, [page, keyword, eventType, username]);

  return (
    <div className="space-y-6 pb-12">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <FileSearch className="text-cyan-400" size={24} />
            <span>Forensic Event Explorer</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Global telemetry search across all case files, authentication logs, and correlated streams.
          </p>
        </div>

        <button
          onClick={loadLogs}
          className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium flex items-center gap-2 border border-slate-700 transition-all cursor-pointer self-start sm:self-auto"
        >
          <RefreshCw size={14} className={loading ? 'animate-spin text-cyan-400' : ''} />
          <span>Refresh Records</span>
        </button>
      </div>

      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 shadow-lg flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="relative w-full md:w-96">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
            <Search size={14} />
          </div>
          <input
            type="text"
            value={keyword}
            onChange={(e) => { setKeyword(e.target.value); setPage(0); }}
            placeholder="Search across verbatim log records..."
            className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-9 pr-3 py-2 text-xs text-white placeholder-slate-500 font-mono focus:border-cyan-400 focus:outline-none"
          />
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto text-xs">
          <select
            value={eventType}
            onChange={(e) => { setEventType(e.target.value); setPage(0); }}
            className="bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-slate-300 font-mono outline-none"
          >
            <option value="">ALL EVENT TYPES</option>
            <option value="LOGIN_SUCCESS">LOGIN_SUCCESS</option>
            <option value="LOGIN_FAILED">LOGIN_FAILED</option>
            <option value="ACCOUNT_LOCKED">ACCOUNT_LOCKED</option>
            <option value="USER_CREATED">USER_CREATED</option>
            <option value="PASSWORD_CHANGED">PASSWORD_CHANGED</option>
            <option value="PRIVILEGE_ESCALATED">PRIVILEGE_ESCALATED</option>
          </select>

          <input
            type="text"
            value={username}
            onChange={(e) => { setUsername(e.target.value); setPage(0); }}
            placeholder="Username filter..."
            className="bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-slate-300 font-mono placeholder-slate-500 w-36 outline-none"
          />
        </div>
      </div>

      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl shadow-xl overflow-hidden backdrop-blur-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-950/60 font-mono text-[11px] uppercase tracking-wider text-slate-400">
                <th className="px-5 py-3.5">ID</th>
                <th className="px-5 py-3.5">Case Dossier</th>
                <th className="px-5 py-3.5">Timestamp</th>
                <th className="px-5 py-3.5">Type</th>
                <th className="px-5 py-3.5">User</th>
                <th className="px-5 py-3.5">Verbatim Message</th>
                <th className="px-5 py-3.5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300 font-sans">
              {logs.map((log) => (
                <tr key={log.id} className="hover:bg-slate-800/40 transition-colors">
                  <td className="px-5 py-3 font-mono text-slate-500 text-[11px]">
                    #{log.id}
                  </td>
                  <td className="px-5 py-3 font-mono text-cyan-400 whitespace-nowrap">
                    INV-{log.investigationId}
                  </td>
                  <td className="px-5 py-3 font-mono text-slate-300 whitespace-nowrap">
                    {log.timestamp}
                  </td>
                  <td className="px-5 py-3 whitespace-nowrap">
                    <span className="font-mono text-[11px] bg-slate-950 text-cyan-300 px-2 py-0.5 rounded border border-slate-800">
                      {log.eventType}
                    </span>
                  </td>
                  <td className="px-5 py-3 font-mono text-white whitespace-nowrap">
                    {log.username || '-'}
                  </td>
                  <td className="px-5 py-3 font-mono text-slate-400 text-[11px] truncate max-w-md">
                    {log.rawMessage}
                  </td>
                  <td className="px-5 py-3 text-right whitespace-nowrap">
                    <button
                      onClick={() => setSelectedLog(log)}
                      className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-cyan-400 font-mono text-[11px] transition-colors cursor-pointer"
                    >
                      Inspect
                    </button>
                  </td>
                </tr>
              ))}
              {logs.length === 0 && (
                <tr>
                  <td colSpan={7} className="p-12 text-center text-slate-500 font-mono text-xs">
                    {loading ? 'Searching forensic records...' : 'No log events found matching current criteria.'}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className="px-5 py-3 border-t border-slate-800 bg-slate-950/50 flex items-center justify-between text-xs font-mono text-slate-400">
          <span>Showing {logs.length} of {totalElements} events</span>
          <div className="flex items-center gap-2">
            <button
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              className="px-3 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 cursor-pointer"
            >
              Previous
            </button>
            <span>Page {page + 1} of {Math.max(1, totalPages)}</span>
            <button
              disabled={page + 1 >= totalPages}
              onClick={() => setPage((p) => p + 1)}
              className="px-3 py-1 rounded bg-slate-800 hover:bg-slate-700 disabled:opacity-40 cursor-pointer"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {selectedLog && (
        <RawLogModal log={selectedLog} onClose={() => setSelectedLog(null)} />
      )}
    </div>
  );
}

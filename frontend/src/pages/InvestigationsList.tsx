import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  FolderLock,
  Search,
  Plus,
  ArrowRight,
  Trash2,
  Calendar,
  User,
  AlertCircle
} from 'lucide-react';
import { investigationApi } from '../api/client';
import { Investigation } from '../types';
import { StatusBadge } from '../components/common/StatusBadge';

export default function InvestigationsList() {
  const [investigations, setInvestigations] = useState<Investigation[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const loadInvestigations = async () => {
    try {
      setLoading(true);
      const data = await investigationApi.getAll(searchQuery);
      setInvestigations(data);
    } catch (err) {
      console.error('Failed to load investigations', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInvestigations();
  }, [searchQuery]);

  const handleDelete = async (id: number, name: string) => {
    if (!window.confirm(`Are you sure you want to delete investigation dossier "${name}"? This action cannot be undone.`)) {
      return;
    }
    try {
      await investigationApi.delete(id);
      setInvestigations((prev) => prev.filter((i) => i.id !== id));
    } catch (err) {
      alert('Failed to delete investigation');
    }
  };

  const filteredInvestigations = investigations.filter((inv) => {
    if (statusFilter === 'ALL') return true;
    return inv.status === statusFilter;
  });

  return (
    <div className="space-y-6 pb-12">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <FolderLock className="text-cyan-400" size={24} />
            <span>Investigation Dossiers</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Catalog of forensic incident cases, associated log evidence, and threat correlations.
          </p>
        </div>

        <Link
          to="/investigations/new"
          className="px-4 py-2.5 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold flex items-center gap-2 shadow-[0_0_15px_rgba(6,182,212,0.3)] transition-all self-start sm:self-auto"
        >
          <Plus size={16} />
          <span>New Investigation</span>
        </Link>
      </div>

      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 shadow-lg backdrop-blur-sm flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="relative w-full md:w-96">
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
            <Search size={16} />
          </div>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search case title, ID, or investigator..."
            className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-10 pr-4 py-2 text-xs text-white placeholder-slate-500 font-mono focus:border-cyan-400 focus:outline-none"
          />
        </div>

        <div className="flex items-center gap-1.5 overflow-x-auto w-full md:w-auto pb-1 md:pb-0 text-xs">
          <span className="text-slate-500 text-[11px] font-mono mr-1 hidden lg:inline">FILTER:</span>
          {['ALL', 'OPEN', 'IN_PROGRESS', 'CLOSED', 'ARCHIVED'].map((st) => (
            <button
              key={st}
              onClick={() => setStatusFilter(st)}
              className={`px-3 py-1.5 rounded-lg font-mono text-[11px] transition-all cursor-pointer whitespace-nowrap ${
                statusFilter === st
                  ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 font-semibold'
                  : 'bg-slate-950 text-slate-400 border border-slate-800 hover:text-white hover:bg-slate-800'
              }`}
            >
              {st}
            </button>
          ))}
        </div>
      </div>

      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl shadow-lg overflow-hidden backdrop-blur-sm">
        {loading ? (
          <div className="p-12 text-center text-xs text-slate-400 font-mono">
            Loading case files...
          </div>
        ) : filteredInvestigations.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/60 font-mono text-[11px] uppercase tracking-wider text-slate-400">
                  <th className="px-6 py-3.5">Case Identifier</th>
                  <th className="px-6 py-3.5">Investigation Name & Scope</th>
                  <th className="px-6 py-3.5">Lead Investigator</th>
                  <th className="px-6 py-3.5">Status</th>
                  <th className="px-6 py-3.5">Initiated</th>
                  <th className="px-6 py-3.5 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-slate-300 font-sans">
                {filteredInvestigations.map((inv) => (
                  <tr
                    key={inv.id}
                    className="hover:bg-slate-800/40 transition-colors group"
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="font-mono font-medium text-cyan-400 bg-cyan-950/40 px-2 py-1 rounded border border-cyan-500/20">
                        {inv.investigationId}
                      </span>
                    </td>
                    <td className="px-6 py-4 max-w-sm">
                      <Link
                        to={`/investigations/${inv.id}`}
                        className="font-semibold text-white group-hover:text-cyan-300 transition-colors block text-sm"
                      >
                        {inv.name}
                      </Link>
                      <p className="text-slate-400 text-xs line-clamp-1 mt-0.5">
                        {inv.description || 'No description provided.'}
                      </p>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center gap-1.5 font-mono text-slate-300">
                        <User size={13} className="text-slate-500" />
                        <span>{inv.investigatorName}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <StatusBadge status={inv.status} />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap font-mono text-slate-400 text-[11px]">
                      <div className="flex items-center gap-1.5">
                        <Calendar size={13} className="text-slate-500" />
                        <span>
                          {inv.createdAt ? new Date(inv.createdAt).toLocaleString() : 'N/A'}
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right">
                      <div className="flex items-center justify-end gap-2">
                        <Link
                          to={`/investigations/${inv.id}`}
                          className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-cyan-600 hover:text-white text-slate-300 font-medium transition-all flex items-center gap-1 text-xs"
                        >
                          <span>Open Dossier</span>
                          <ArrowRight size={13} />
                        </Link>
                        <button
                          onClick={() => handleDelete(inv.id, inv.name)}
                          title="Delete Dossier"
                          className="p-1.5 text-slate-500 hover:text-red-400 hover:bg-slate-800 rounded-lg transition-colors"
                        >
                          <Trash2 size={15} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="p-12 text-center space-y-3">
            <div className="w-12 h-12 rounded-full bg-slate-800 flex items-center justify-center mx-auto text-slate-500">
              <AlertCircle size={24} />
            </div>
            <h3 className="text-base font-bold text-white">No Investigations Found</h3>
            <p className="text-xs text-slate-400 max-w-sm mx-auto">
              {searchQuery || statusFilter !== 'ALL'
                ? 'No case records matched your current query or filter criteria.'
                : 'No forensic investigations have been initiated yet. Create a case to begin ingesting evidence.'}
            </p>
            <Link
              to="/investigations/new"
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold transition-all mt-2"
            >
              <Plus size={15} />
              <span>Create Investigation</span>
            </Link>
          </div>
        )}
      </div>
    </div>
  );
}

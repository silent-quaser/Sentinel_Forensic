import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  FolderPlus,
  UploadCloud,
  FileText,
  AlertTriangle,
  Terminal,
  ArrowRight
} from 'lucide-react';
import { investigationApi, logApi } from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function CreateInvestigation() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [name, setName] = useState('Brute Force Authentication & Incident Analysis');
  const [investigatorName, setInvestigatorName] = useState(user?.fullName || 'Naveen (Investigator)');
  const [description, setDescription] = useState(
    'Forensic examination of repeated authentication failures, subsequent lockout, and timeline reconstruction across internal perimeter servers.'
  );
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [filePreview, setFilePreview] = useState<{ lines: number; sample: string[] } | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleFileSelection = (file: File) => {
    setSelectedFile(file);
    const reader = new FileReader();
    reader.onload = (e) => {
      const text = (e.target?.result as string) || '';
      const lines = text.split('\n').filter((l) => l.trim().length > 0);
      setFilePreview({
        lines: lines.length,
        sample: lines.slice(0, 5),
      });
    };
    reader.readAsText(file);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFileSelection(e.dataTransfer.files[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    setSubmitting(true);
    setError(null);

    try {
      const inv = await investigationApi.create({
        name: name.trim(),
        investigatorName: investigatorName.trim(),
        description: description.trim(),
      });

      if (selectedFile) {
        await logApi.upload(inv.id, selectedFile);
      }

      navigate(`/investigations/${inv.id}`);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to initiate investigation');
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6 pb-12">
      <div>
        <div className="flex items-center gap-2 mb-1">
          <FolderPlus className="text-cyan-400" size={24} />
          <h1 className="text-2xl font-bold text-white tracking-tight">
            Initiate Forensic Investigation
          </h1>
        </div>
        <p className="text-xs text-slate-400">
          Register a new forensic case file, assign lead investigator credentials, and ingest initial log evidence.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {error && (
          <div className="p-4 rounded-xl bg-red-950/60 border border-red-500/40 text-red-300 text-xs flex items-center gap-3">
            <AlertTriangle size={18} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-lg backdrop-blur-sm space-y-4">
          <h3 className="text-xs uppercase font-bold tracking-wider text-slate-400 flex items-center gap-1.5">
            <Terminal size={14} className="text-cyan-400" />
            <span>Case File Parameters</span>
          </h3>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Investigation Title
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Brute Force Authentication Analysis"
              className="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-white font-mono focus:border-cyan-400 focus:outline-none"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Lead Investigator
              </label>
              <input
                type="text"
                required
                value={investigatorName}
                onChange={(e) => setInvestigatorName(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-white font-mono focus:border-cyan-400 focus:outline-none"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Classification Level
              </label>
              <input
                type="text"
                disabled
                value="RESTRICTED - FORENSIC EVIDENCE"
                className="w-full bg-slate-950/50 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-slate-500 font-mono"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Incident Scope & Description
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detail incident context, suspicious systems, suspected intrusion vector..."
              className="w-full bg-slate-950 border border-slate-700 rounded-xl p-3.5 text-xs text-white placeholder-slate-500 focus:border-cyan-400 focus:outline-none resize-none"
            />
          </div>
        </div>

        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-lg backdrop-blur-sm space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-xs uppercase font-bold tracking-wider text-slate-400 flex items-center gap-1.5">
              <UploadCloud size={14} className="text-cyan-400" />
              <span>Ingest Initial Forensic Log (Optional)</span>
            </h3>
            <span className="text-[11px] text-slate-500 font-mono">
              Supports .log, .txt files
            </span>
          </div>

          <div
            onDrop={handleDrop}
            onDragOver={handleDragOver}
            className={`border-2 border-dashed rounded-2xl p-8 text-center transition-all cursor-pointer ${
              selectedFile
                ? 'border-cyan-500/60 bg-cyan-950/20'
                : 'border-slate-700 hover:border-cyan-500/40 bg-slate-950/60'
            }`}
            onClick={() => document.getElementById('log-file-input')?.click()}
          >
            <input
              id="log-file-input"
              type="file"
              accept=".log,.txt"
              className="hidden"
              onChange={(e) => {
                if (e.target.files && e.target.files.length > 0) {
                  handleFileSelection(e.target.files[0]);
                }
              }}
            />

            <UploadCloud
              size={36}
              className={`mx-auto mb-3 transition-colors ${
                selectedFile ? 'text-cyan-400' : 'text-slate-500'
              }`}
            />

            {selectedFile ? (
              <div className="space-y-1">
                <p className="text-sm font-bold text-white font-mono">
                  {selectedFile.name}
                </p>
                <p className="text-xs text-cyan-400 font-mono">
                  {(selectedFile.size / 1024).toFixed(1)} KB &bull; {filePreview?.lines ?? 0} log lines detected
                </p>
                <p className="text-[11px] text-slate-400 mt-2">
                  Click to choose a different evidence file
                </p>
              </div>
            ) : (
              <div className="space-y-1">
                <p className="text-sm font-semibold text-slate-300">
                  Drag & drop your security log file here, or browse
                </p>
                <p className="text-xs text-slate-500">
                  Accepts standard timestamped audit logs (e.g. sample_system.log)
                </p>
              </div>
            )}
          </div>

          {filePreview && (
            <div className="bg-slate-950 p-4 rounded-xl border border-slate-800/80">
              <div className="flex items-center justify-between mb-2">
                <span className="text-[11px] font-mono font-semibold uppercase text-cyan-400 flex items-center gap-1.5">
                  <FileText size={12} />
                  <span>Log Sample Preview</span>
                </span>
                <span className="text-[11px] font-mono text-slate-500">First 5 lines</span>
              </div>
              <pre className="text-[11px] font-mono text-slate-300 overflow-x-auto whitespace-pre-wrap space-y-0.5">
                {filePreview.sample.map((line, idx) => (
                  <div key={idx} className="py-0.5">
                    <span className="text-slate-600 select-none mr-2">{idx + 1}:</span>
                    {line}
                  </div>
                ))}
              </pre>
            </div>
          )}
        </div>

        <div className="flex items-center justify-end gap-4">
          <button
            type="button"
            onClick={() => navigate('/investigations')}
            className="px-5 py-2.5 text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl transition-colors cursor-pointer"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="px-6 py-2.5 rounded-xl bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white font-semibold text-xs shadow-[0_0_20px_rgba(6,182,212,0.3)] flex items-center gap-2 transition-all cursor-pointer disabled:opacity-50"
          >
            <span>{submitting ? 'Initializing Case & Ingesting...' : 'Initiate Investigation'}</span>
            <ArrowRight size={15} />
          </button>
        </div>
      </form>
    </div>
  );
}

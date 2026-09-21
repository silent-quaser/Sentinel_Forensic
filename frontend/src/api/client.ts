import axios from 'axios';
import {
  Investigation,
  CreateInvestigationPayload,
  LogEntry,
  TimelineEvent,
  Threat,
  ThreatRule,
  InvestigationNote,
  DashboardSummary,
  SystemHealth,
  DetectionResult,
  LogUploadResult,
  PageResponse
} from '../types';

const apiBase = import.meta.env.VITE_API_URL
  ? (import.meta.env.VITE_API_URL.endsWith('/api')
      ? import.meta.env.VITE_API_URL
      : `${import.meta.env.VITE_API_URL}/api`)
  : '/api';

export const apiClient = axios.create({
  baseURL: apiBase,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const investigationApi = {
  getAll: async (search?: string) => {
    const params = search ? { search } : {};
    const response = await apiClient.get<Investigation[]>('/investigations', { params });
    return response.data;
  },

  getById: async (id: number) => {
    const response = await apiClient.get<Investigation>(`/investigations/${id}`);
    return response.data;
  },

  create: async (payload: CreateInvestigationPayload) => {
    const response = await apiClient.post<Investigation>('/investigations', payload);
    return response.data;
  },

  updateStatus: async (id: number, status: string) => {
    const response = await apiClient.patch<Investigation>(`/investigations/${id}/status`, { status });
    return response.data;
  },

  delete: async (id: number) => {
    await apiClient.delete(`/investigations/${id}`);
  },

  downloadPdfReport: async (id: number, filename = 'SentinelForensic_Report.pdf') => {
    const response = await apiClient.get(`/investigations/${id}/report`, {
      responseType: 'blob',
    });
    const blob = new Blob([response.data], { type: 'application/pdf' });
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(downloadUrl);
  },

  getNotes: async (id: number) => {
    const response = await apiClient.get<InvestigationNote[]>(`/investigations/${id}/notes`);
    return response.data;
  },

  addNote: async (id: number, author: string, content: string) => {
    const response = await apiClient.post<InvestigationNote>(`/investigations/${id}/notes`, { author, content });
    return response.data;
  },
};

export const logApi = {
  upload: async (investigationId: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post<LogUploadResult>(
      `/investigations/${investigationId}/logs`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  },

  getInvestigationLogs: async (
    investigationId: number,
    params?: {
      eventType?: string;
      username?: string;
      source?: string;
      keyword?: string;
      startTime?: string;
      endTime?: string;
      page?: number;
      size?: number;
    }
  ) => {
    const response = await apiClient.get<PageResponse<LogEntry>>(
      `/investigations/${investigationId}/logs`,
      { params }
    );
    return response.data;
  },

  getTimeline: async (investigationId: number) => {
    const response = await apiClient.get<TimelineEvent[]>(`/investigations/${investigationId}/timeline`);
    return response.data;
  },

  searchGlobalLogs: async (params?: {
    eventType?: string;
    username?: string;
    source?: string;
    keyword?: string;
    page?: number;
    size?: number;
  }) => {
    const response = await apiClient.get<PageResponse<LogEntry>>('/logs', { params });
    return response.data;
  },
};

export const threatApi = {
  runDetection: async (investigationId: number) => {
    const response = await apiClient.post<DetectionResult>(`/investigations/${investigationId}/detect`);
    return response.data;
  },

  getInvestigationThreats: async (investigationId: number) => {
    const response = await apiClient.get<Threat[]>(`/investigations/${investigationId}/threats`);
    return response.data;
  },

  getAllThreats: async (params?: {
    investigationId?: number;
    severity?: string;
    status?: string;
    ruleCode?: string;
    username?: string;
  }) => {
    const response = await apiClient.get<Threat[]>('/threats', { params });
    return response.data;
  },

  getThreatDetail: async (threatId: number) => {
    const response = await apiClient.get<Threat>(`/threats/${threatId}`);
    return response.data;
  },

  updateStatus: async (threatId: number, status: string) => {
    const response = await apiClient.patch<Threat>(`/threats/${threatId}/status`, { status });
    return response.data;
  },
};

export const ruleApi = {
  getAll: async () => {
    const response = await apiClient.get<ThreatRule[]>('/rules');
    return response.data;
  },

  getByCode: async (code: string) => {
    const response = await apiClient.get<ThreatRule>(`/rules/${code}`);
    return response.data;
  },

  update: async (
    id: number,
    payload: { enabled?: boolean; thresholdCount?: number; windowMinutes?: number }
  ) => {
    const response = await apiClient.patch<ThreatRule>(`/rules/${id}`, payload);
    return response.data;
  },
};

export const dashboardApi = {
  getSummary: async () => {
    const response = await apiClient.get<DashboardSummary>('/dashboard/summary');
    return response.data;
  },
};

export const healthApi = {
  getHealth: async () => {
    const response = await apiClient.get<SystemHealth>('/health');
    return response.data;
  },
};

export default apiClient;

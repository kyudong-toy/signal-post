import axios from "axios";
import {useAuthStore} from "@/entities/user/model/authStore.ts";

const baseURL = import.meta.env.VITE_API_BASE_URL || `http://localhost:8080/api/v1`;

const axiosClient = axios.create({
  baseURL: baseURL
});

axiosClient.interceptors.request.use(
  (config) => {
    const {accessToken} = useAuthStore.getState();

    if (accessToken) {
      config.headers['Authorization'] = `Bearer ${accessToken}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error)
  }
);

export default axiosClient;
import type {FileUploadRes} from "@/entities/file/model/types.ts";
import axiosClient from "@/shared/axios";

export const fileUpload = async (file: File): Promise<FileUploadRes> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await axiosClient.post<FileUploadRes>('/files/temp', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    }
  });

  return response.data;
}
/**
 * 联系人 DTO（对齐后端 `supplier_contact` 表）。
 */
export interface ContactDto {
  id: number;
  supplierId?: number;
  name: string;
  phone: string;
  email: string;
  primary: boolean;
  position?: string | null;
  department?: string | null;
  createdAt?: string;
}

/**
 * 新增/编辑联系人请求 DTO（id 为空表示新增）。
 */
export interface SaveContactRequest {
  name: string;
  phone: string;
  email: string;
  primary: boolean;
  position?: string;
  department?: string;
}

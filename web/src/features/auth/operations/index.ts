"use server";

import { redirect } from "next/navigation";
import { api } from "@/infrastructure/api/client";
import { setSession, clearSession } from "@/infrastructure/auth/session";
import type { LoginInput, RegisterInput } from "../schemas";

interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

interface AuthResult {
  error?: string;
}

export async function loginAction(input: LoginInput): Promise<AuthResult> {
  try {
    const tokens = await api.post<AuthTokens>("/v1/auth/login", input);
    await setSession(tokens.accessToken, tokens.refreshToken);
  } catch (err: unknown) {
    const message =
      err instanceof Error ? err.message : "Đăng nhập thất bại";
    return { error: message };
  }
  redirect("/feed");
}

export async function registerAction(input: RegisterInput): Promise<AuthResult> {
  try {
    const tokens = await api.post<AuthTokens>("/v1/auth/register", input);
    await setSession(tokens.accessToken, tokens.refreshToken);
  } catch (err: unknown) {
    const message =
      err instanceof Error ? err.message : "Đăng ký thất bại";
    return { error: message };
  }
  redirect("/feed");
}

export async function logoutAction(): Promise<void> {
  try {
    await api.post("/v1/auth/logout");
  } catch {
    // best-effort
  }
  await clearSession();
  redirect("/login");
}

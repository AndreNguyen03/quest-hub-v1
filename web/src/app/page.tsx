import { redirect } from "next/navigation";
import { isAuthenticated } from "@/infrastructure/auth/session";

export default async function RootPage() {
  if (await isAuthenticated()) {
    redirect("/feed");
  }
  redirect("/login");
}

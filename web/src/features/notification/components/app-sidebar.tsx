/**
 * AppSidebar — server component that reads auth session.
 * Wrapped in <Suspense> by parent layout (required for runtime APIs in Next.js 16).
 */
import Link from "next/link";
import { Bell, BookOpen, Globe, MessageSquare, Sparkles, Users } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { api } from "@/infrastructure/api/client";
import { getAccessToken } from "@/infrastructure/auth/session";
import { logoutAction } from "@/features/auth/operations";
import { Button } from "@/components/ui/button";

interface UnreadCount {
  count: number;
}

const NAV_ITEMS = [
  { href: "/feed", label: "Feed", icon: Users },
  { href: "/quests", label: "Quests", icon: BookOpen },
  { href: "/world", label: "World", icon: Globe },
  { href: "/coach", label: "AI Coach", icon: Sparkles },
  { href: "/notifications", label: "Thông báo", icon: Bell },
];

export async function AppSidebar() {
  const token = await getAccessToken();
  if (!token) return null;

  // Fetch unread notification count for badge
  let unreadCount = 0;
  try {
    const data = await api.get<UnreadCount>("/v1/notifications/unread-count");
    unreadCount = data.count;
  } catch {
    // badge is non-critical
  }

  return (
    <aside className="w-64 shrink-0 border-r flex flex-col h-screen sticky top-0 p-4">
      <Link href="/feed" className="text-xl font-bold mb-8 block">
        ⚔️ QuestHub
      </Link>

      <nav className="flex-1 space-y-1">
        {NAV_ITEMS.map(({ href, label, icon: Icon }) => (
          <Link
            key={href}
            href={href}
            className="flex items-center gap-3 px-3 py-2 rounded-md text-sm hover:bg-accent transition-colors"
          >
            <Icon className="h-4 w-4" />
            <span>{label}</span>
            {href === "/notifications" && unreadCount > 0 && (
              <Badge variant="destructive" className="ml-auto text-xs">
                {unreadCount > 99 ? "99+" : unreadCount}
              </Badge>
            )}
          </Link>
        ))}
      </nav>

      <form action={logoutAction}>
        <Button variant="ghost" size="sm" className="w-full justify-start" type="submit">
          Đăng xuất
        </Button>
      </form>
    </aside>
  );
}

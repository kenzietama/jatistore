import React from 'react';
import { SellerSidebar } from './SellerSidebar';
import { SellerHeader } from './SellerHeader';

interface SellerLayoutProps {
  children: React.ReactNode;
}

export function SellerLayout({ children }: SellerLayoutProps) {
  return (
    <div className="bg-background text-on-background font-body-md min-h-screen flex">
      <SellerSidebar />
      <main className="flex-1 ml-0 md:ml-[240px] p-gutter max-w-container-max mx-auto w-full">
        <SellerHeader />
        {children}
      </main>
    </div>
  );
}

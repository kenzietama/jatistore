import React from 'react';

export function WelcomeSection() {
  return (
    <section className="mb-stack-lg flex justify-between items-end">
      <div>
        <h2 className="font-headline-lg text-headline-lg text-on-surface mb-unit">Welcome back, Store Owner.</h2>
        <p className="font-body-md text-body-md text-on-surface-variant">Here's a quick overview of your store's performance today.</p>
      </div>
    </section>
  );
}

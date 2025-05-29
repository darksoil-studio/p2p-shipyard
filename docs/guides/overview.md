# Overview

Distributed apps have some unique qualities:

- Offline-first, they work even when internet connectivity is unreliable.
- Resilient to censorship, making it difficult for centralized actors to can shut them down.
- Transparent and immutable, perfectly suited for cases where an ecosystem of organizations benefits from verifiable accountability.

But! Building peer-to-peer apps can be really hard. Having a distributed network of peers instead of a centralized server that you maintain makes it hard to understand and monitor the state of the app.

Furthermore, there aren't a lot of resources out there to help developers have a smooth building experience.

This is why built p2p Shipyard. **It's an all-included development toolkit for building peer-to-peer apps**.

## Tech stack

Our goal is to create performant, resilient and flexible peer-to-peer apps, that can deliver a simple and smooth user experience to our users.

To that end, these are the tools that we chose:

- We use [holochain](https://developer.holochain.org) as our underlying protocol to build peer-to-peer apps.

- We use [tauri](https://tauri.app) to distribute apps for both desktop and mobile targets.

- We use web technologies like javascript, HTML and to build the frontend client, and we use rust to build the backend of apps.

## Building

Excited to start building? Move on to [creating an app](/guides/creating-an-app) to start the journey of building a peer-to-peer app.

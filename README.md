# MarketPlace 

**MarketPlace** is a Minecraft plugin designed for Spigot version 1.20.4 that allows players to create, list, and purchase custom items in a dynamic marketplace. The plugin integrates with MySQL for data storage and Redis for caching, ensuring fast and reliable performance. It also supports a Black Market feature, transaction history, and more.

## Features

- **Custom Item Listings**: Players can list custom items for sale in the marketplace.
- **Marketplace GUI**: A user-friendly graphical interface to view, list, and purchase items.
- **Black Market**: Items on the Black Market are randomly selected and sold at a 50% discount, with sellers getting reimbursed 2x for their items.
- **Transaction History**: Displays the most recent 9 transactions in a custom GUI, which updates as new purchases are made.
- **Discord Webhooks**: Log each purchase through a Discord webhook for external tracking.
- **Fully Configurable**: Messages, menus, permissions, and other settings are customizable through configuration files.
- **MySQL & Redis Integration**: Uses MySQL for persistent data storage and Redis for quick caching.
- **Permission-Based Access**: All features are controlled by permissions, giving server owners full control over access.

## Installation

1. Download the latest release from the [Releases](https://github.com/Ampsz/MarketPlace/releases) page.
2. Place the `.jar` file into your server's `/plugins/` directory.
3. Restart or reload your server to generate the configuration files.
4. Configure the plugin by editing the `config.yml` and other configuration files as needed.
5. Set up MySQL and Redis in your environment for database storage and caching.
6. Use the appropriate commands to start using the MarketPlace plugin!

## Commands

- `/sell <item>`: Lists an item for sale in the marketplace.
- `/marketplace`: Opens the Marketplace GUI, showing all available items for purchase.
- `/blackmarket`: Opens the Black Market GUI with discounted items.
- `/blackmarket sell`: Sells items directly to the Black Market.
- `/transactions`: Shows the transaction history of recent purchases.
- `/reloadmarketplace`: Reloads the plugin's configuration files.
- `/wipedatabase`: Wipes all data from the database (use with caution!).

## Permissions

- `marketplace.use`: Allows access to the main marketplace.
- `marketplace.sell`: Allows selling items on the marketplace.
- `marketplace.blackmarket`: Allows access to the Black Market feature.
- `marketplace.admin`: Allows administrative actions like wiping the database or reloading configurations.

## Configuration

The plugin comes with several configurable options, allowing you to customize the behavior of the marketplace to fit your server's needs. Configuration files include:

- `config.yml`: Main configuration for database connections, Redis setup, and general settings.
- `messages.yml`: Customizable messages for in-game interactions.
- `menus.yml`: Adjust menu sizes, items per page, and background items for both the marketplace and the black market.
  
To reload the configurations without restarting the server, use `/reloadmarketplace`.

## Database Setup

This plugin requires MySQL for persistent storage and Redis for caching. In `config.yml`, specify your MySQL and Redis connection details:

```yaml
mysql:
  host: "your-mysql-host"
  port: 3306
  database: "marketplace"
  username: "your-username"
  password: "your-password"

redis:
  host: "your-redis-host"
  port: 6379

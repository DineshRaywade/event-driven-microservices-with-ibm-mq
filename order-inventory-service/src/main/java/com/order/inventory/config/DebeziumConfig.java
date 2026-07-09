package com.order.inventory.config;


import io.debezium.config.Configuration;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class DebeziumConfig {

    @Bean
    public Configuration debeziumConnectorConfig() {

        return Configuration.create()

                .with("name", "inventory-outbox-connector")

                .with(
                        "connector.class",
                        "io.debezium.connector.postgresql.PostgresConnector"
                )

                .with(
                        "offset.storage",
                        "org.apache.kafka.connect.storage.FileOffsetBackingStore"
                )

                .with(
                        "offset.storage.file.filename",
                        "/tmp/debezium-offsets-inventory.dat"
                )

                .with("offset.flush.interval.ms", "1000")

                .with("database.hostname", "localhost")

                .with("database.port", "5432")

                .with("database.user", "postgres")

                .with("database.password", "postgres")

                .with("database.dbname", "inventory_db")

                .with("topic.prefix", "inventory")

                .with("plugin.name", "pgoutput")

                .with("slot.name", "inventory_outbox_slot")

                .with(
                        "table.include.list",
                        "public.outbox_events"
                )

                .build();
    }
}

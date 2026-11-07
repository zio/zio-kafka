const sidebars = {
  sidebar: [
    {
      type: "category",
      label: "ZIO Kafka",
      collapsed: false,
      link: { type: "doc", id: "index" },
      items: [
        {
          type: "category",
          label: "Getting Started",
          collapsed: false,
          items: [
            "tutorial",
            "creating-a-consumer"
          ]
        },
        {
          type: "category",
          label: "Consuming",
          collapsed: false,
          items: [
            "consuming-kafka-topics-using-zio-streams",
            "partition-assignment-and-offset-retrieval",
            "sharing-consumer",
            "preventing-duplicates",
            "avoiding-chunk-breakers"
          ]
        },
        {
          type: "category",
          label: "Producing and Serialization",
          collapsed: false,
          items: [
            "consuming-producing-and-committing-offsets",
            "serialization-and-deserialization",
            "transactions"
          ]
        },
        {
          type: "category",
          label: "Operations",
          collapsed: false,
          items: [
            "consumer-tuning",
            "metrics",
            "detecting-authorization-failures"
          ]
        },
        {
          type: "category",
          label: "Testing and Migration",
          collapsed: false,
          items: [
            "writing-tests",
            "migrating-to-zio-kafka-3"
          ]
        }
      ]
    }
  ]
};

module.exports = sidebars;

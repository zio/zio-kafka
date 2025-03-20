const sidebars = {
  sidebar: [
    {
      type: "category",
      label: "ZIO Kafka",
      collapsed: false,
      link: { type: "doc", id: "index" },
      items: [
        "creating-a-consumer",
        "consuming-kafka-topics-using-zio-streams",
        "example-of-consuming-producing-and-committing-offsets",
        "partition-assignment-and-offset-retrieval",
        "metrics",
        "consumer-tuning",
        "avoiding-chunk-breakers",
        "preventing-duplicates",
        "sharing-consumer",
        "serialization-and-deserialization",
        "transactions",
        "writing-tests",
        "migrating-to-zio-kafka-3"
      ]
    }
  ]
};

module.exports = sidebars;

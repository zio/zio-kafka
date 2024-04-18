const sidebars = {
  sidebar: [
    {
      type: "category",
      label: "ZIO Kafka",
      collapsed: false,
      link: { type: "doc", id: "index" },
      items: [
        "consuming-kafka-topics-using-zio-streams",
        "example-of-consuming-producing-and-committing-offsets",
        "partition-assignment-and-offset-retrieval",
        "consumer-tuning",
        "preventing-duplicates",
        "sharing-consumer",
        "serialization-and-deserialization",
        "writing-tests"
      ]
    }
  ]
};

module.exports = sidebars;

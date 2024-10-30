# DVHetero

DVHetero includes:
1. **SoC**: It is the SoC Generator.
2. **SoCDiff**: This tool is used for simulating and debugging SoCs.

## Getting Started

### Dependencies

- JDK 8 or newer (for SoC)
- SBT (for SoC)
- CMake (for SoCDiff)
- C++-11 (for SoCDiff)
- Verilator (for SoCDiff)

### Clone the Repository

```bash
git clone https://github.com/JuliusGrey/DVHetero
cd DVHetero
```

## SoC

### Generate CGRA's Verilog

```bash
cd SoC
```

Using SBT command:

```bash
sbt "runMain CGRA.ARCH.moduleIns.CGRAGen"
```

### Generate SoC's Verilog

```bash
cd SoC
```

Using SBT command:

```bash
sbt "runMain sbt "runMain TOP.TopOKGen"
```

## SoCDiff

Copy your SoC into SoCDiff: move the generated TopOK.v to the DVHetero/SoCDiff/npc/socv directory.

The /SoCDiff/am-kernels/kernels directory contains some differential testing examples that can be used for differential testing of SoCs. For example:

```bash
cd /SoCDiff/am-kernels/kernels/pedometersf
make run
```




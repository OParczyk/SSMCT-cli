import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as ml
import numpy as np
import os
import sys

def ssmatrix(file,splitter,comparator,name="matrix.png",endian='big'):    

    matrix=[]
    in_stream=[]

    print("Read file")
    with open(file, "rb") as f:
        byte = f.read(1)
        while byte:
            in_stream.append(byte)
            byte = f.read(1)

    print("File read")
    print("Start splitting")
    unclean_chunks=splitter(in_stream)
    print("File split")
    
    print("Clean data")
    chunks = [x for x in unclean_chunks if x]

    resolution=len(chunks)
    print("Resolution will be "+str(resolution))

    print("Start comparison")
    for i in chunks:
        matrix.append(comparator(i,chunks))
    print("Finished comparison")

    print("Generating Image")
    fig=ml.figure(figsize=(np.ceil(resolution/100),np.ceil(resolution/100)), frameon=False)
    ax = fig.add_axes([0, 0, 1, 1])
    ax.axis('off')
    ml.pcolormesh(matrix, figure=fig)

    print("Saving Image")
    fig.savefig(name)
    ml.close()

def byte_by_byte_comparator(compare_chunk,all_chunks):
    #compares chunks byte by byte, for equity and same relative position
    #if chunksizes differ, behavior is undefined
    ret=[]
    for i in range(len(all_chunks)):
        value=0
        for j in range(np.minimum(len(compare_chunk),len(all_chunks[i]))):
            if all_chunks[i][j] == compare_chunk[j]: value+=1
        ret.append(value)
        current_chunk=[]
    return ret

def exactly_the_same(compare_chunk,all_chunks):
    ret=[]
    for i in all_chunks:
        if i==compare_chunk: ret.append(1)
        else: ret.append(0)
    return ret

def split_at_number_of_bytes(in_stream,chunk_size=256):
    ret=[]
    current_chunk=[]
    for i in in_stream:
        current_chunk.append(i)
        if len(current_chunk)==chunk_size:
            ret.append(current_chunk)
            current_chunk=[]
    if len(current_chunk)!=0: ret.append(current_chunk)
    return ret

def split_at_bytes(in_stream,bytes_to_split_at=[b'\n',b' ',b',', b'.',b';',b'{',b'}',b'"',b'\'',b'(',b')'],remove_splitting_byte=True):
    ret=[]
    current_chunk=[]
    for i in in_stream:
        if i in bytes_to_split_at:
            if not remove_splitting_byte: current_chunk.append(i)
            ret.append(current_chunk)
            current_chunk=[]
            continue
        current_chunk.append(i)
    if len(current_chunk)!=0: ret.append(current_chunk)
    return ret
    

ssmatrix(sys.argv[1],split_at_number_of_bytes,byte_by_byte_comparator)
